package com.agentstudy.statistics.dto;

import java.util.List;

public record AgentCallStatisticsResponse(
        int sampleSize,
        long successCount,
        long failedCount,
        double successRate,
        double averageDurationMillis,
        List<AgentTypeCallStatisticsResponse> byAgentType
) {
}
