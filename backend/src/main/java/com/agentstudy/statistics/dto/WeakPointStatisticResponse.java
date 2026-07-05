package com.agentstudy.statistics.dto;

public record WeakPointStatisticResponse(
        String code,
        String name,
        String latestReason,
        long count
) {
}
