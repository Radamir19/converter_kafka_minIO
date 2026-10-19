package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.repository.InboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxRepository inboxRepository;

    @Transactional
    public boolean claim(UUID eventId) {
        inboxRepository.tryInsert(eventId);
        return inboxRepository.tryProcess(eventId) == 1;
    }

    @Transactional
    public void release(UUID eventId) {
        inboxRepository.release(eventId);
    }

    @Transactional
    public void markError(UUID eventId) {
        inboxRepository.setErrorStatus(eventId);
    }

    @Transactional
    public void processing(UUID eventId) {
        inboxRepository.tryProcess(eventId);
    }

    @Transactional
    public boolean success(UUID eventId) {
        return inboxRepository.tryUpdate(eventId) == 1;
    }
}
