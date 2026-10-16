package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.convert.ConverterStrategy;
import org.example.converterservice.convert.FileNames;
import org.example.converterservice.minio_worker.MinioWorker;
import org.example.converterservice.model.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConverterService {
    private final Map<String, ConverterStrategy> strategies = new HashMap<>();
    private final MinioWorker worker;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public ConverterService(List<ConverterStrategy> all, MinioWorker worker,
                            ObjectMapper objectMapper, OutboxRepository outboxRepository) {
        for(ConverterStrategy c : all) {
            strategies.put(c.extension(), c);
        }
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
