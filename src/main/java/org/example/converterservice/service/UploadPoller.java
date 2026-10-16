package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.minio_worker.MinioWorker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadPoller {
    private final MinioWorker worker;
    private final ConverterService service;

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        String bucket = "test";
        try {
            List<String> keys = worker.listKeys(bucket, "upload/");
            log.info("В {}/upload/ найдено файлов: {} -> {}", bucket, keys.size(), keys);
            for(String key : keys) {
                try {
                    service.convertFile(key, bucket);
                    worker.delete(key, bucket);
                    log.info("Сконвертирован {}", key);
                } catch (Exception e) {
                    log.warn("Не удалось сконвертировать {} : {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Не удалось прочитать список файлов: {}", e.getMessage());
        }
    }
}
