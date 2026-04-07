package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<Todo> searchTodosQuery(String weather, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        // QueryDSL 버전 검색 메서드. 과제 3번 최종 정리는 JPQL로 했지만,
        // 동적 조건 처리 예시로 남겨둔 구현이다.
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                // 연관 user를 같이 가져와서 N+1을 피한다.
                .leftJoin(todo.user, user).fetchJoin()
                .where(
                        weatherEq(weather, todo),
                        modifiedAtGoe(startDate, todo),
                        modifiedAtLoe(endDate, todo))
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        weatherEq(weather, todo),
                        modifiedAtGoe(startDate, todo),
                        modifiedAtLoe(endDate, todo))
                .fetchOne();

        return new PageImpl<>(todos, pageable, total == null ? 0 : total);
    }


    private BooleanExpression weatherEq(String weather, QTodo todo) {
        if (weather == null || weather.isBlank()) {
            return null;
        }
        return todo.weather.eq(weather);
    }

    private BooleanExpression modifiedAtGoe(LocalDateTime startDate, QTodo todo) {
        return startDate != null ? todo.modifiedAt.goe(startDate) : null;
    }

    private BooleanExpression modifiedAtLoe(LocalDateTime endDate, QTodo todo) {
        return endDate != null ? todo.modifiedAt.loe(endDate) : null;
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest request, Pageable pageable) {

        // 과제 10번: 제목, 기간, 담당자 nickname 조건으로 통계성 검색 결과를 만든다.
        List<TodoSearchResponse> responses = queryFactory
                .select(new QTodoSearchResponse(
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(manager).on(manager.todo.eq(todo))
                .leftJoin(comment).on(comment.todo.eq(todo))
                .leftJoin(manager.user, user)
                .where(
                        titleContains(request.getTitle()),
                        createdAtGoe(request.getStartTime()),
                        createdAtLoe(request.getEndTime()),
                        nicknameContains(request.getNickname())
                )
                .groupBy(todo.id, todo.title)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.id.countDistinct())
                .from(todo)
                .leftJoin(manager).on(manager.todo.eq(todo))
                .leftJoin(manager.user, user)
                .where(
                        titleContains(request.getTitle()),
                        createdAtGoe(request.getStartTime()),
                        createdAtLoe(request.getEndTime()),
                        nicknameContains(request.getNickname())
                )
                .fetchOne();

        return new PageImpl<>(responses, pageable, total == null ? 0 : total);
    }


    private BooleanExpression titleContains(String title) {
        return title == null || title.isBlank() ? null : todo.title.containsIgnoreCase(title);
    }

    private BooleanExpression createdAtGoe(LocalDateTime startTime) {
        return startTime == null ? null : todo.createdAt.goe(startTime);
    }

    private BooleanExpression createdAtLoe(LocalDateTime endTime) {
        return endTime == null ? null : todo.createdAt.loe(endTime);
    }

    private BooleanExpression nicknameContains(String nickname) {
        return nickname == null || nickname.isBlank() ? null : user.nickname.containsIgnoreCase(nickname);
    }

    @Override
    public Optional<Todo> getTodo(Long todoId) {

        // 과제 8번: Todo 단건 조회를 QueryDSL + fetchJoin으로 전환
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }


}
