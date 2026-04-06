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