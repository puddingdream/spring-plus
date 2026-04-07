package org.example.expert.domain.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveManagerRegisterLog(
            Long requestUserId,
            Long managerUserId,
            Long todoId,
            LogStatus status,
            String message
    ) {
        // 과제 11번 핵심: REQUIRES_NEW로 별도 트랜잭션을 열어
        // 바깥 비즈니스 로직이 실패/롤백되어도 로그는 독립적으로 남긴다.
        Log log = new Log(
                "MANAGER_REGISTER",
                requestUserId,
                 managerUserId,
                todoId,
                status,
                message
        );

        logRepository.save(log);
    }
}
