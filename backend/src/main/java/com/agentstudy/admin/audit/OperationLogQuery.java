package com.agentstudy.admin.audit;

public record OperationLogQuery(
        int limit,
        String operator,
        String action,
        String targetType,
        String targetId
) {
}
