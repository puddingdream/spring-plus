package org.example.expert.domain.todo.dto.request;

import java.time.LocalDateTime;

public record TodoGetRequest(
        String weather,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
