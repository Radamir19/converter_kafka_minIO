package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.model.status.InboxStatus;
import org.example.converterservice.model.status.OutboxStatus;
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
    private final InboxService inboxService;

    @Transactional
    public void create(UUID eventId, FileConvertedEvent event) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregate(event.sourceKey());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        outbox.setStatus(OutboxStatus.NEW.name());
        inboxService.updateStatus(eventId, List.of(InboxStatus.IN_PROCESS.name()), InboxStatus.PROCESSED.name());
        outboxRepository.save(outbox);
    }

    @Transactional
    public List<OutboxEntity> claimBatch(int limit) {
        return outboxRepository.claimBatch(limit);
    }

    @Transactional
    public void finishBatch(List<UUID> sentIds, List<UUID> failedIds, Short maxAttempts) {
        outboxRepository.markSent(sentIds);
        outboxRepository.markFailed(failedIds, maxAttempts);
    }
}
