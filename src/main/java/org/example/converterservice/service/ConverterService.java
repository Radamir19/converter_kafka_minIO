package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.convert.ConverterStrategy;
import org.example.converterservice.convert.FileNames;
import org.example.converterservice.minio_worker.MinioWorker;
import org.example.converterservice.model.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class ConverterService {
    private final Map<String, ConverterStrategy> strategies;
    private final MinioWorker worker;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public ConverterService(@Qualifier("converters") Map<String, ConverterStrategy> strategies,
                            MinioWorker worker,
                            ObjectMapper objectMapper,
                            OutboxRepository outboxRepository) {
        this.strategies = strategies;
        this.worker = worker;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }


    @Transactional
    public void convertFile(String key, String bucket) throws Exception {
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
            OutboxEntity outbox = new OutboxEntity();
            outbox.setId(UUID.randomUUID());
            outbox.setAggregate(key);
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outboxRepository.save(outbox);
        }
    }
}
