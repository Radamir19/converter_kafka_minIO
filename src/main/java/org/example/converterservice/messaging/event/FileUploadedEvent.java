package org.example.converterservice.messaging.event;

import java.util.UUID;

public record FileUploadedEvent(UUID eventId, String bucket, String key) {
}
