package com.agentstudy.agent.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record RunAgentRequest(
        String sessionId,
        @NotBlank(message = "promptCode is required")
        String promptCode,
        Map<String, Object> variables
) {
}
