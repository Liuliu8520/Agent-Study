package com.agentstudy.agent;

public record RenderedPrompt(
        String code,
        AgentType agentType,
        String version,
        String systemPrompt,
        String userPrompt
) {
}
