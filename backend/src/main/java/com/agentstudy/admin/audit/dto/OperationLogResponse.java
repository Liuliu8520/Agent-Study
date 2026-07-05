package com.agentstudy.admin.audit.dto;

import com.agentstudy.admin.audit.OperationLog;
import java.time.Instant;

public record OperationLogResponse(
        String logId,
        String operator,
        String action,
        String targetType,
        String targetId,
        String detail,
        Instant createdAt
) {

    public static OperationLogResponse from(OperationLog log) {
        return new OperationLogResponse(
                log.logId(),
                log.operator(),
                log.action(),
                log.targetType(),
                log.targetId(),
                log.detail(),
                log.createdAt()
        );
    }
}
