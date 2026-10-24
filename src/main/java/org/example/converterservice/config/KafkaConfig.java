package org.example.converterservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.example.converterservice.messaging.event.FileUploadedEvent;
import org.example.converterservice.service.InboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public NewTopic fileCreatedTopic(@Value("${app.kafka.topics.converted}") String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }

    @Bean
    public NewTopic receiveDltTopic(@Value("${app.kafka.topics.receive}") String receive) {
        return TopicBuilder.name(receive + ".DLT")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template,
                                                 InboxService service,
                                                 ObjectMapper objectMapper) {
        DeadLetterPublishingRecoverer dlq = new DeadLetterPublishingRecoverer(template);
        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            dlq.accept(record, ex);
            try{
                FileUploadedEvent event = objectMapper.readValue((String) record.value(), FileUploadedEvent.class);
                if(event.eventId() != null) {
                    service.updateStatus(event.eventId(), List.of("RECEIVED", "IN_PROCESS"), "ERROR");
                }
            } catch (Exception e) {
                log.error("Не удалось пометить событие как ERROR", e);
            }
        };
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        return handler;
    }
}
