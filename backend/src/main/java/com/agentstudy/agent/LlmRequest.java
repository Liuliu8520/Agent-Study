package com.agentstudy.agent;

import java.util.Map;

public record LlmRequest(
        String sessionId,
        AgentType agentType,
        String promptCode,
        String promptVersion,
        String systemPrompt,
        String userPrompt,
        Map<String, Object> metadata
) {
}
