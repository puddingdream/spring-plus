package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryCustom {

    Page<Todo> searchTodosQuery(
            String weather,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);


    Page<TodoSearchResponse> searchTodos(
            TodoSearchRequest request,
            Pageable pageable);

    Optional<Todo> getTodo(@Param("todoId") Long todoId);
}
