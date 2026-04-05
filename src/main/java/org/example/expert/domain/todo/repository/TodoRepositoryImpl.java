package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<Todo> searchTodosQuery(String weather, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user,user).fetchJoin()
                .where(
                        weatherEq(weather,todo),
                        modifiedAtGoe(startDate,todo),
                        modifiedAtLoe(endDate,todo))
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
       if(weather == null || weather.isBlank()){
           return null;
       }
        return todo.weather.eq(weather);
    }

    private BooleanExpression modifiedAtGoe(LocalDateTime startDate, QTodo todo){
        return startDate != null ? todo.modifiedAt.goe(startDate) : null;
    }

    private BooleanExpression modifiedAtLoe(LocalDateTime endDate, QTodo todo){
        return endDate != null ? todo.modifiedAt.loe(endDate) : null;
    }
}
