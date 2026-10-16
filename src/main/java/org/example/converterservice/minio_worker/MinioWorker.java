package org.example.converterservice.minio_worker;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioWorker {
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

    public List<String> listKeys(String bucket, String prefix) throws Exception {
        List<String> keys = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
        for(Result<Item> result : results) {
            Item item = result.get();
            if(!item.isDir()) {
                keys.add(item.objectName());
            }
        }
        return keys;
    }

    public void delete(String key, String bucket) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                .build());
    }
}
