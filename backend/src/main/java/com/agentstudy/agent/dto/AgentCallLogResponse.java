package com.agentstudy.agent.dto;

import com.agentstudy.agent.AgentCallLog;
import com.agentstudy.agent.AgentCallStatus;
import com.agentstudy.agent.AgentType;
import java.time.Instant;

public record AgentCallLogResponse(
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

    public static AgentCallLogResponse from(AgentCallLog log) {
        return new AgentCallLogResponse(
                log.callId(),
                log.sessionId(),
                log.agentType(),
                log.promptCode(),
                log.promptVersion(),
                log.modelName(),
                log.requestPayload(),
                log.responseText(),
                log.status(),
                log.errorMessage(),
                log.durationMillis(),
                log.createdAt()
        );
    }
}
