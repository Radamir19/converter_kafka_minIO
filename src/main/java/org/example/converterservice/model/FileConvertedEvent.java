package org.example.converterservice.model;

public record FileConvertedEvent(String sourceKey, String resultKey, String bucket) {
}
