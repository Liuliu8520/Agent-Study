package com.agentstudy.statistics.dto;

public record SessionStatusStatisticsResponse(
        int totalSessions,
        int createdSessions,
        int inProgressSessions,
        int finishedSessions
) {
}
