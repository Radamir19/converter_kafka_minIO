package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.model.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, FileConvertedEvent> template;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void publishPending() {
        List<OutboxEntity> batch = outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();

        for(OutboxEntity outbox : batch) {
            try {
                FileConvertedEvent event = objectMapper.readValue(outbox.getPayload(), FileConvertedEvent.class);
                template.send("file-created", event)
                        .get(10, TimeUnit.SECONDS);
                outbox.setPublishedAt(Instant.now().atOffset(ZoneOffset.UTC));
                outboxRepository.save(outbox);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.warn("Outbox send failed, retry on next tick: {}", e.getMessage());
                break;
            }
        }
    }
}
