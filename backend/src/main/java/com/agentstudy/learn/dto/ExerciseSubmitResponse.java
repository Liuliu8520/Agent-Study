package com.agentstudy.learn.dto;

import java.util.List;

public record ExerciseSubmitResponse(
        String sessionId,
        int correctCount,
        int totalCount,
        double errorRate,
        List<ExerciseJudgeResultResponse> results,
        int currentStep,
        String nextAction
) {
}

