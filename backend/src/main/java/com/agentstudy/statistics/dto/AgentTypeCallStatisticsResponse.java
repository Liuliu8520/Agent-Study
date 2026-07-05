package com.agentstudy.statistics.dto;

import com.agentstudy.agent.AgentType;

public record AgentTypeCallStatisticsResponse(
        AgentType agentType,
        long callCount,
        double averageDurationMillis
) {
}
