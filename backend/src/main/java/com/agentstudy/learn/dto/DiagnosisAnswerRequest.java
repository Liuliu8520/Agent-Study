package com.agentstudy.learn.dto;

import jakarta.validation.constraints.NotBlank;

public record DiagnosisAnswerRequest(
        @NotBlank(message = "Question id is required")
        String questionId,

        @NotBlank(message = "Selected option is required")
        String selectedOption
) {
}

