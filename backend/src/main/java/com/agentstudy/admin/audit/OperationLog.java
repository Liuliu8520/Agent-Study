package com.agentstudy.admin.audit;

import java.time.Instant;

public record OperationLog(
        String logId,
        String operator,
        String action,
        String targetType,
        String targetId,
        String detail,
        Instant createdAt
) {
}
