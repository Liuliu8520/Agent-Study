package com.agentstudy.learn.dto;

import jakarta.validation.constraints.NotBlank;

public record ExerciseAnswerRequest(
        @NotBlank(message = "Question id is required")
        String questionId,

        @NotBlank(message = "Answer expression is required")
        String answerExpression
) {
}

