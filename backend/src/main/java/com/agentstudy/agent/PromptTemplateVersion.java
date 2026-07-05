package com.agentstudy.agent;

import java.time.Instant;

public record PromptTemplateVersion(
        String versionId,
        String code,
        AgentType agentType,
        String version,
        String name,
        String systemPrompt,
        String userPromptTemplate,
        boolean active,
        String createdBy,
        Instant createdAt
) {

    public PromptTemplate toTemplate() {
        return new PromptTemplate(code, agentType, version, name, systemPrompt, userPromptTemplate);
    }
}
