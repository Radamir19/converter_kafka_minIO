package org.example.converterservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.model.FileUploadedEvent;
import org.example.converterservice.service.ConverterService;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FileReceiveListener {
    private final ConverterService service;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"receive_topic"}, groupId = "converter-service")
    public void onMessage(String message) throws Exception {
        FileUploadedEvent event = objectMapper.readValue(message, FileUploadedEvent.class);
        log.info("Получено {}/{}", event.bucket(), event.key());
        service.convertFile(event.key(), event.bucket());
    }
}
