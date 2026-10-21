package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.data.domain.Limit;
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
    private final InboxService inboxService;

    @Transactional
    public void create(UUID eventId, FileConvertedEvent event) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregate(event.sourceKey());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        outbox.setStatus("NEW");
        inboxService.updateStatus(eventId, List.of("IN_PROCESS"), "PROCESSEd");
        outboxRepository.save(outbox);
    }

    @Transactional
    public List<OutboxEntity> claimBatch() {
        return outboxRepository.claimBatch();
    }

    @Transactional
    public void finishBatch(List<UUID> sentIds, List<UUID> failedIds, Short maxAttempts) {
        outboxRepository.markSent(sentIds);
        outboxRepository.markFailed(failedIds, maxAttempts);
    }
}
