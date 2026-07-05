package com.agentstudy.agent.dto;

import com.agentstudy.agent.AgentType;
import com.agentstudy.agent.PromptTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertPromptTemplateRequest(
        @NotNull AgentType agentType,
        @NotBlank String version,
        @NotBlank String name,
        @NotBlank String systemPrompt,
        @NotBlank String userPromptTemplate
) {

    public PromptTemplate toTemplate(String code) {
        return new PromptTemplate(
                code,
                agentType,
                version.trim(),
                name.trim(),
                systemPrompt,
                userPromptTemplate
        );
    }
}
