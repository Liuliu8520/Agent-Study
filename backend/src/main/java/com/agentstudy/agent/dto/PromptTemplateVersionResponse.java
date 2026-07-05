package com.agentstudy.agent.dto;

import com.agentstudy.agent.AgentType;
import com.agentstudy.agent.PromptTemplateVersion;
import java.time.Instant;

public record PromptTemplateVersionResponse(
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

    public static PromptTemplateVersionResponse from(PromptTemplateVersion version) {
        return new PromptTemplateVersionResponse(
                version.versionId(),
                version.code(),
                version.agentType(),
                version.version(),
                version.name(),
                version.systemPrompt(),
                version.userPromptTemplate(),
                version.active(),
                version.createdBy(),
                version.createdAt()
        );
    }
}
