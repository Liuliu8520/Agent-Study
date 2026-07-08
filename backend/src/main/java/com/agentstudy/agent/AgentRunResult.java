package com.agentstudy.agent;

import java.time.Instant;

public record AgentRunResult(
        String callId,
        String sessionId,
        AgentType agentType,
        String promptCode,
        String promptVersion,
        String modelName,
        String outputText,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        long durationMillis,
        Instant createdAt
) {

    public static AgentRunResult from(AgentCallLog log) {
        return new AgentRunResult(
                log.callId(),
                log.sessionId(),
                log.agentType(),
                log.promptCode(),
                log.promptVersion(),
                log.modelName(),
                log.responseText(),
                log.promptTokens(),
                log.completionTokens(),
                log.totalTokens(),
                log.durationMillis(),
                log.createdAt()
        );
    }
}
