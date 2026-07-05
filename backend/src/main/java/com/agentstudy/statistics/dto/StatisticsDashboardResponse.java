package com.agentstudy.statistics.dto;

import java.util.List;

public record StatisticsDashboardResponse(
        SessionStatusStatisticsResponse sessions,
        List<WeakPointStatisticResponse> weakPoints,
        AgentCallStatisticsResponse agentCalls
) {
}
