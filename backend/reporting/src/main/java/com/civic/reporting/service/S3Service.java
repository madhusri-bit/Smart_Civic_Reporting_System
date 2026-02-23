package com.civic.reporting.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region:ap-south-1}")
    private String region;

    public String uploadFile(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "jpg";
        if (original != null && original.contains(".")) {
            String[] parts = original.split("\\.");
            ext = parts[parts.length - 1];
        }

        String key = "issues/" + UUID.randomUUID().toString() + "." + ext;

        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(por, RequestBody.fromBytes(file.getBytes()));

        // Public URL (assumes bucket/object ACL or bucket policy allows public read)
        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
        return url;
    }
}
