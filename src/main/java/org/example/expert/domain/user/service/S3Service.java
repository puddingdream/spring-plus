package org.example.expert.domain.user.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    private final S3Template s3Template;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadProfileImage(MultipartFile file) {
        validateFile(file);

        try {
            String key = "profiles/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .build();

            s3Template.upload(bucket, key, file.getInputStream(), metadata);
            return key;
        } catch (IOException e) {
            throw new ServerException("파일 업로드에 실패했습니다.");
        }
    }

    public URL getDownloadUrl(String key) {
        return s3Template.createSignedGetURL(bucket, key, PRESIGNED_URL_EXPIRATION);
    }

    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServerException("업로드할 파일이 없습니다.");
        }
    }
}
