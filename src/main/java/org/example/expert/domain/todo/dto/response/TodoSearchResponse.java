package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record TodoSearchResponse(
        String title,
        Long managerCount,
        Long commentCount) {
    @QueryProjection
    public TodoSearchResponse {
    }
}
