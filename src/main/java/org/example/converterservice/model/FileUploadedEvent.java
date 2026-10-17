package org.example.converterservice.model;

public record FileUploadedEvent(String bucket, String key) {
}
