package com.agentstudy.agent;

public record AgentCallLogQuery(
        int limit,
        String sessionId,
        AgentType agentType,
        AgentCallStatus status,
        String promptCode
) {
}
