package com.agentstudy.agent;

public record LlmResponse(
        String modelName,
        String content,
        int promptTokens,
        int completionTokens
) {
}
