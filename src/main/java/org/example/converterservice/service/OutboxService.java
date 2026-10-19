package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final InboxService service;

    @Transactional
    public void create(UUID eventId, FileConvertedEvent event) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregate(event.sourceKey());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        service.success(eventId);
        outboxRepository.save(outbox);
    }

    public List<OutboxEntity> findTop10ByStatusOrderByCreatedAtAsc() {
        return outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("NEW");
    }
    @Transactional
    public boolean sending(UUID id) {
        return outboxRepository.trySend(id) == 1;
    }

    @Transactional
    public void update(UUID id) {
        outboxRepository.tryUpdate(id);
    }

    @Transactional
    public void release(UUID id, Short maxAttempts) {
        outboxRepository.markFailed(id, maxAttempts);
    }

}
