package com.agentstudy.agent;

import java.time.Instant;

public record AgentCallLog(
        String callId,
        String sessionId,
        AgentType agentType,
        String promptCode,
        String promptVersion,
        String modelName,
        String requestPayload,
        String responseText,
        AgentCallStatus status,
        String errorMessage,
        long durationMillis,
        Instant createdAt
) {
}
