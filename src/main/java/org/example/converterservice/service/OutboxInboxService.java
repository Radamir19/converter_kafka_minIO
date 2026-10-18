package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.messaging.event.FileConvertedEvent;
import org.example.converterservice.model.InboxEntity;
import org.example.converterservice.model.OutboxEntity;
import org.example.converterservice.repository.InboxRepository;
import org.example.converterservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxInboxService {
    private final OutboxRepository outboxRepository;
    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void create(UUID eventId, FileConvertedEvent event) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregate(event.sourceKey());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        int check = inboxRepository.tryInsert(eventId);
        if(check == 0) {
            return;
        }
        outboxRepository.save(outbox);
    }
}
