package com.agentstudy.learn.exercise;

import java.time.Instant;
import java.util.List;

public record ExerciseAttempt(
        String attemptId,
        String sessionId,
        int correctCount,
        int totalCount,
        double errorRate,
        List<ExerciseJudgeResult> results,
        Instant submittedAt
) {
}
