package com.agentstudy.admin.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogService {

    private final OperationLogRepository repository;

    public OperationLogService(OperationLogRepository repository) {
        this.repository = repository;
    }

    public OperationLog record(String operator, String action, String targetType, String targetId, String detail) {
        return repository.save(new OperationLog(
                UUID.randomUUID().toString(),
                StringUtils.hasText(operator) ? operator : "system",
                action,
                targetType,
                targetId,
                detail,
                Instant.now()
        ));
    }

    public List<OperationLog> search(int limit, String operator, String action, String targetType, String targetId) {
        return repository.search(new OperationLogQuery(
                Math.min(Math.max(limit, 1), 100),
                operator,
                action,
                targetType,
                targetId
        ));
    }
}
