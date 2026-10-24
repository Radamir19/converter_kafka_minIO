package org.example.converterservice.messaging.event;

public record FileConvertedEvent(String sourceKey, String resultKey, String bucket) {
}
