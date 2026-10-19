package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
        List<OutboxEntity> batch = outboxService.findTop10ByStatusOrderByCreatedAtAsc();

        for (OutboxEntity outbox : batch) {
            UUID id = outbox.getId();
            if (outboxService.sending(id)) {
                continue;
            }
            FileConvertedEvent event = objectMapper.readValue(outbox.getPayload(), FileConvertedEvent.class);

            template.send(convertedTopic, outbox.getAggregate(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxService.update(id);
                        } else {
                            outboxService.release(id, maxAttempts);
                            log.warn("Событие {} не отправлено, повтор на следующем тике", id, ex);
                        }
                    });
        }
    }
}
