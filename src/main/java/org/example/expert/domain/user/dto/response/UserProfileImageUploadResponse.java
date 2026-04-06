package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserProfileImageUploadResponse {

    private final String key;

    public UserProfileImageUploadResponse(String key) {
        this.key = key;
    }
}
