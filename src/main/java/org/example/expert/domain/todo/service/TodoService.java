package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.log.LogService;
import org.example.expert.domain.log.LogStatus;
import org.example.expert.domain.todo.dto.request.TodoGetRequest;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    // 과제 11번: 매니저 등록 로그를 별도 트랜잭션으로 남기기 위해 사용
    private final LogService logService;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo savedTodo = null;
        try {
            // 과제 6번: Todo 생성자 안에서 작성자를 자동 담당자로 등록하도록 cascade persist와 연결
            Todo newTodo = new Todo(
                    todoSaveRequest.getTitle(),
                    todoSaveRequest.getContents(),
                    weather,
                    user
            );
            savedTodo = todoRepository.save(newTodo);
            // 성공 로그는 REQUIRES_NEW 트랜잭션으로 저장되어 메인 로직과 분리된다.
            logService.saveManagerRegisterLog(
                    authUser.getId(),
                    authUser.getId(),
                    savedTodo.getId(),
                    LogStatus.SUCCESS,
                    "자동 담당자 등록 성공"
            );
            return new TodoSaveResponse(
                    savedTodo.getId(),
                    savedTodo.getTitle(),
                    savedTodo.getContents(),
                    weather,
                    new UserResponse(user.getId(), user.getEmail())
            );
        } catch (RuntimeException e) {
            // 메인 트랜잭션이 실패해도 실패 로그는 따로 남긴다.
            logService.saveManagerRegisterLog(
                    authUser.getId(),
                    authUser.getId(),
                    null,
                    LogStatus.FAIL,
                    e.getMessage()
            );
            throw e;
        }
    }

    public Page<TodoResponse> getTodos(int page, int size, TodoGetRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 과제 3번: JPQL 기반 optional 검색 조건 사용
        Page<Todo> todos = todoRepository.searchTodosJPQL(request.weather(), request.startDate(), request.endDate(), pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }


    public TodoResponse getTodo(long todoId) {
        // 과제 8번: QueryDSL 구현체의 fetchJoin 조회 사용
        Todo todo = todoRepository.getTodo(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest request) {
        // 과제 10번: QueryDSL 기반 검색 API
        return todoRepository.searchTodos(request, request.toPageable());
    }
}
