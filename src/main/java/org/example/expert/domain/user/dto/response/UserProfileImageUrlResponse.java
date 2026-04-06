package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserProfileImageUrlResponse {

    private final String url;

    public UserProfileImageUrlResponse(String url) {
        this.url = url;
    }
}
