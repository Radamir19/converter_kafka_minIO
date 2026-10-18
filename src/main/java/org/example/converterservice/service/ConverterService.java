package org.example.converterservice.service;

import org.example.converterservice.service.convert.ConverterStrategy;
import org.example.converterservice.service.util.FileNames;
import org.example.converterservice.storage.MinioStorage;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class ConverterService {
    private final Map<String, ConverterStrategy> strategies;
    private final MinioStorage worker;
    private final OutboxInboxService outboxService;

    public ConverterService(@Qualifier("converters") Map<String, ConverterStrategy> strategies,
                            MinioStorage worker,
                            OutboxInboxService outboxService) {
        this.strategies = strategies;
        this.worker = worker;
        this.outboxService = outboxService;
    }


    public void convertFile(UUID eventId, String key, String bucket) throws Exception {
        String extension = FileNames.extensionOf(key);
        ConverterStrategy strategy = strategies.get(extension);
        if(strategy == null) {
            throw new IllegalArgumentException("Формат не поддерживается: " + extension);
        }
        try(InputStream inputStream = worker.download(key, bucket)) {
            byte[] result = strategy.convert(inputStream);
            String resultKey = "converted/" + FileNames.withoutExtension(key) + "." + strategy.outputExtension();
            worker.upload(result, resultKey, bucket, strategy.outputContentType());
            FileConvertedEvent event = new FileConvertedEvent(key, resultKey, bucket);
            outboxService.create(eventId, event);
        }
    }
}
