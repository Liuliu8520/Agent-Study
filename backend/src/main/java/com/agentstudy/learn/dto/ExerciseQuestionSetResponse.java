package com.agentstudy.learn.dto;

import java.util.List;

public record ExerciseQuestionSetResponse(
        String sessionId,
        int currentStep,
        String nextAction,
        List<ExerciseQuestionResponse> questions
) {
}

