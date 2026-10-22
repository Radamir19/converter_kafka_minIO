package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {
    private final OutboxService outboxService;
    private final KafkaTemplate<String, FileConvertedEvent> template;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.converted}")
    private String convertedTopic;
    @Value("${app.outbox.max-attempts}")
    private Short maxAttempts;

    @Scheduled(fixedDelay = 1000)
    public void publishPending() {
        List<OutboxEntity> batch = outboxService.claimBatch(10);
        List<UUID> sentIds = new ArrayList<>();
        List<UUID> failedIds = new ArrayList<>();
        Map<UUID, CompletableFuture<?>> pending = new HashMap<>();
        for (OutboxEntity outbox : batch) {
            try {
                FileConvertedEvent event = objectMapper.readValue(outbox.getPayload(), FileConvertedEvent.class);
                pending.put(outbox.getId(), template.send(convertedTopic, outbox.getAggregate(), event));
            } catch (Exception e) {
                failedIds.add(outbox.getId());
                log.warn("Не удалось отправить {}", outbox.getId(), e);
            }
        }

        for (Map.Entry<UUID, CompletableFuture<?>> entry : pending.entrySet()) {
            try {
                entry.getValue().get(10, TimeUnit.SECONDS);
                sentIds.add(entry.getKey());
            } catch (Exception e) {
                failedIds.add(entry.getKey());
                log.warn("Событие {} не отправлено, повтор на следующем тике", entry.getKey(), e);
            }
        }

        outboxService.finishBatch(sentIds, failedIds, maxAttempts);

    }
}
