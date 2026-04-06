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

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("/users/search")
    public ResponseEntity<UserResponse> getUserByNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.getUserByNickname(nickname));
    }

    @PutMapping("/users")
    public void changePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserChangePasswordRequest userChangePasswordRequest
    ) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @PostMapping("/users/profile-image")
    public ResponseEntity<UserProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userService.uploadProfileImage(authUser.getId(), file));
    }

    @GetMapping("/users/profile-image")
    public ResponseEntity<UserProfileImageUrlResponse> getProfileImageDownloadUrl(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(userService.getProfileImageDownloadUrl(authUser.getId()));
    }
}
