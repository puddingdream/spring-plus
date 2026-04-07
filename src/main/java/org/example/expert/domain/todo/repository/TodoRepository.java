package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    // 과제 3번: weather, startDate, endDate를 모두 optional 조건으로 처리
    @Query("""
        SELECT t
        FROM Todo t
        LEFT JOIN FETCH t.user u
        where (:weather is null or t.weather = :weather)
        and (:startDate is null or t.modifiedAt >= :startDate)
        and (:endDate is null or t.modifiedAt <= :endDate)
        ORDER BY t.modifiedAt DESC
        """)
    Page<Todo> searchTodosJPQL(
            @Param("weather") String weather,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 과제 8번 이전 단계에서 사용하던 사용자 조인 조회 메서드
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
