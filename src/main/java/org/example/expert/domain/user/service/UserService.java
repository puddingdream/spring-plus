package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileImageUploadResponse;
import org.example.expert.domain.user.dto.response.UserProfileImageUrlResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // 실제 S3 연동 책임은 별도 서비스에 위임해서, UserService는 "유저 정보와 연결"에 집중한다.
    private final S3Service s3Service;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    // 과제 13번: nickname exact match 검색
    public UserResponse getUserByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    @Transactional
    public UserProfileImageUploadResponse uploadProfileImage(long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // 파일 자체는 S3에 저장하고, DB에는 object key만 저장한다.
        // 이렇게 하면 DB에는 큰 바이너리를 넣지 않고도 파일 위치를 추적할 수 있다.
        String key = s3Service.uploadProfileImage(file);
        user.updateProfileImageKey(key);

        return new UserProfileImageUploadResponse(key);
    }

    public UserProfileImageUrlResponse getProfileImageDownloadUrl(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (user.getProfileImageKey() == null || user.getProfileImageKey().isBlank()) {
            throw new InvalidRequestException("Profile image not found");
        }

        // 클라이언트가 S3에서 직접 다운로드할 수 있도록 presigned URL을 내려준다.
        // 서버가 파일 바이트를 중계하지 않아도 되어 트래픽 부담을 줄일 수 있다.
        return new UserProfileImageUrlResponse(
                s3Service.getDownloadUrl(user.getProfileImageKey()).toString()
        );
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        // 단순 길이 체크만 하지 않고, 숫자/대문자 포함 조건도 같이 검증한다.
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }
}
