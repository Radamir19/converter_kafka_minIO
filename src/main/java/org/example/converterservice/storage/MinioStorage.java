package org.example.converterservice.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MinioStorage {
    private final MinioClient minioClient;

    public void upload(byte[] data, String key, String bucketName, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(key)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .contentType(contentType)
                        .build());
    }

    public InputStream download(String key, String bucketName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(key)
                        .build());
    }
}
