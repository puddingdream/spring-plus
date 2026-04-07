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

    // presigned URL은 너무 길면 유출 리스크가 커지고, 너무 짧으면 사용성이 떨어져 10분으로 설정했다.
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    private final S3Template s3Template;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadProfileImage(MultipartFile file) {
        validateFile(file);

        try {
            // 원본 파일명을 그대로 key로 쓰면 충돌 가능성이 있어 UUID를 앞에 붙인다.
            String key = "profiles/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            // content-type을 함께 저장해 두면 S3에서 내려줄 때 브라우저/클라이언트가 파일 형식을 해석하기 쉽다.
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .build();

            // MultipartFile의 InputStream을 바로 S3로 전달한다.
            // 업로드가 성공하면 나중에 DB에 저장할 식별자 역할의 key를 반환한다.
            s3Template.upload(bucket, key, file.getInputStream(), metadata);
            return key;
        } catch (IOException e) {
            throw new ServerException("파일 업로드에 실패했습니다.");
        }
    }

    public URL getDownloadUrl(String key) {
        // presigned URL은 일정 시간 동안만 유효한 다운로드 링크다.
        // private bucket이어도 이 URL을 가진 클라이언트는 만료 전까지 직접 파일을 받을 수 있다.
        return s3Template.createSignedGetURL(bucket, key, PRESIGNED_URL_EXPIRATION);
    }

    private static void validateFile(MultipartFile file) {
        // 빈 파일 업로드는 저장할 가치가 없고, 이후 로직도 모두 무의미해져 초기에 차단한다.
        if (file == null || file.isEmpty()) {
            throw new ServerException("업로드할 파일이 없습니다.");
        }
    }
}
