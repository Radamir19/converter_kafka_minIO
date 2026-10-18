package org.example.converterservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.messaging.event.FileUploadedEvent;
import org.example.converterservice.repository.InboxRepository;
import org.example.converterservice.service.ConverterService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileReceiveListener {
    private final ConverterService service;
    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;

    @KafkaListener(topics = "${app.kafka.topics.receive}")
    public void onMessage(String message) throws Exception {
        FileUploadedEvent event = objectMapper.readValue(message, FileUploadedEvent.class);
        if(event.eventId() == null) {
            throw new IllegalArgumentException("Id события не передали, поэтому файл не конвертируется");
        }
        if(inboxRepository.existsById(event.eventId())) {
            return;
        }
        log.info("Получено {}/{}", event.bucket(), event.key());
        service.convertFile(event.eventId(), event.key(), event.bucket());
    }
}
