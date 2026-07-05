package com.agentstudy.agent.dto;

import com.agentstudy.agent.AgentType;
import com.agentstudy.agent.PromptTemplate;

public record PromptTemplateResponse(
        String code,
        AgentType agentType,
        String version,
        String name,
        String systemPrompt,
        String userPromptTemplate
) {

    public static PromptTemplateResponse from(PromptTemplate template) {
        return new PromptTemplateResponse(
                template.code(),
                template.agentType(),
                template.version(),
                template.name(),
                template.systemPrompt(),
                template.userPromptTemplate()
        );
    }
}
