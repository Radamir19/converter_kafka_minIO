package org.example.converterservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.model.status.InboxStatus;
import org.example.converterservice.service.convert.ConverterStrategy;
import org.example.converterservice.service.util.FileNames;
import org.example.converterservice.storage.MinioStorage;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ConverterService {
    private final Map<String, ConverterStrategy> strategies;
    private final MinioStorage worker;
    private final OutboxService outboxService;
    private final InboxService inboxService;


    public ConverterService(@Qualifier("converters") Map<String, ConverterStrategy> strategies,
                            MinioStorage worker,
                            OutboxService outboxService,
                            InboxService inboxService) {
        this.strategies = strategies;
        this.worker = worker;
        this.outboxService = outboxService;
        this.inboxService = inboxService;
    }


    public void convertFile(UUID eventId, String key, String bucket) throws Exception {
        if(!inboxService.claim(eventId, List.of("RECEIVED"), "IN_PROCESS")) {
            log.info("Событие {} уже обработано или обрабатывается", eventId);
            return;
        }
        try {
            String extension = FileNames.extensionOf(key);
            ConverterStrategy strategy = strategies.get(extension);
            if (strategy == null) {
                throw new IllegalArgumentException("Формат не поддерживается: " + extension);
            }
            try (InputStream inputStream = worker.download(key, bucket)) {
                byte[] result = strategy.convert(inputStream);
                String resultKey = "converted/" + eventId + "/" + FileNames.withoutExtension(key) + "." + strategy.outputExtension();
                worker.upload(result, resultKey, bucket, strategy.outputContentType());
                FileConvertedEvent event = new FileConvertedEvent(key, resultKey, bucket);
                outboxService.create(eventId, event);
            }
        } catch (IllegalArgumentException e) {
            inboxService.updateStatus(eventId, List.of(InboxStatus.IN_PROCESS.name(), InboxStatus.RECEIVED.name()), InboxStatus.ERROR.name());
        } catch (Exception e) {
            inboxService.updateStatus(eventId, List.of(InboxStatus.IN_PROCESS.name()), InboxStatus.RECEIVED.name());
        }
    }
}
