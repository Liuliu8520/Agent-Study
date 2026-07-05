package com.agentstudy.agent;

public record PromptTemplate(
        String code,
        AgentType agentType,
        String version,
        String name,
        String systemPrompt,
        String userPromptTemplate
) {
}
