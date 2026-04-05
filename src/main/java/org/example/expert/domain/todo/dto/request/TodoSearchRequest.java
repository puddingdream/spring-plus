package org.example.expert.domain.todo.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TodoSearchRequest {

    private Integer page = 1;
    private Integer size = 10;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String nickname;

    public Pageable toPageable() {
        int safePage = Math.max(pageOrDefault(), 1) - 1;
        return PageRequest.of(safePage, sizeOrDefault());
    }

    private int pageOrDefault() {
        return page == null ? 1 : page;
    }

    private int sizeOrDefault() {
        return size == null ? 10 : size;
    }
}
