package org.example.expert.domain.log;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;

@Getter
@Entity
@Table(name = "log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private Long requestUserId;
    private Long managerUserId;
    private Long todoId;
    @Enumerated(EnumType.STRING)
    private LogStatus status;
    private String message;

    public Log(String action, Long requestUserId, Long managerUserId, Long todoId, LogStatus status, String message) {
        this.action = action;
        this.requestUserId = requestUserId;
        this.managerUserId = managerUserId;
        this.todoId = todoId;
        this.status = status;
        this.message = message;
    }
}
