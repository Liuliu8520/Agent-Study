package com.agentstudy.agent;

import java.util.Map;

public record AgentRunCommand(
        String sessionId,
        String promptCode,
        Map<String, Object> variables
) {
}
