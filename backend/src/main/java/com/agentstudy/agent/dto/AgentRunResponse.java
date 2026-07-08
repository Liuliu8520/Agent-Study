package com.agentstudy.agent.dto;

import com.agentstudy.agent.AgentRunResult;
import com.agentstudy.agent.AgentType;
import java.time.Instant;

public record AgentRunResponse(
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

    public static AgentRunResponse from(AgentRunResult result) {
        return new AgentRunResponse(
                result.callId(),
                result.sessionId(),
                result.agentType(),
                result.promptCode(),
                result.promptVersion(),
                result.modelName(),
                result.outputText(),
                result.promptTokens(),
                result.completionTokens(),
                result.totalTokens(),
                result.durationMillis(),
                result.createdAt()
        );
    }
}
