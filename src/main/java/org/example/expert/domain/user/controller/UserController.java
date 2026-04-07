package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileImageUploadResponse;
import org.example.expert.domain.user.dto.response.UserProfileImageUrlResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 기본 유저 단건 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 과제 13번: nickname 정확 일치 조건으로 사용자를 조회하는 API
    @GetMapping("/users/search")
    public ResponseEntity<UserResponse> getUserByNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.getUserByNickname(nickname));
    }

    // 인증된 사용자의 비밀번호 변경
    @PutMapping("/users")
    public void changePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserChangePasswordRequest userChangePasswordRequest
    ) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    // 과제 12번: S3에 프로필 이미지를 업로드하고, S3 object key를 사용자 엔티티에 저장한다.
    @PostMapping("/users/profile-image")
    public ResponseEntity<UserProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userService.uploadProfileImage(authUser.getId(), file));
    }

    // 저장된 S3 object key를 기준으로 presigned download URL을 발급한다.
    @GetMapping("/users/profile-image")
    public ResponseEntity<UserProfileImageUrlResponse> getProfileImageDownloadUrl(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(userService.getProfileImageDownloadUrl(authUser.getId()));
    }
}
