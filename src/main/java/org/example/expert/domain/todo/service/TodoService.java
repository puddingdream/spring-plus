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
    private final LogService logService;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo savedTodo = null;
        try {
            Todo newTodo = new Todo(
                    todoSaveRequest.getTitle(),
                    todoSaveRequest.getContents(),
                    weather,
                    user
            );
            savedTodo = todoRepository.save(newTodo);
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
        return todoRepository.searchTodos(request, request.toPageable());
    }
}
