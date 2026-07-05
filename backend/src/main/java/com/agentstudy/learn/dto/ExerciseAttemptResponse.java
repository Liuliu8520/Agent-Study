package com.agentstudy.learn.dto;

import com.agentstudy.learn.exercise.ExerciseAttempt;
import java.time.Instant;
import java.util.List;

public record ExerciseAttemptResponse(
        String attemptId,
        String sessionId,
        int correctCount,
        int totalCount,
        double errorRate,
        List<ExerciseJudgeResultResponse> results,
        Instant submittedAt
) {

    public static ExerciseAttemptResponse from(ExerciseAttempt attempt) {
        return new ExerciseAttemptResponse(
                attempt.attemptId(),
                attempt.sessionId(),
                attempt.correctCount(),
                attempt.totalCount(),
                attempt.errorRate(),
                attempt.results().stream()
                        .map(ExerciseJudgeResultResponse::from)
                        .toList(),
                attempt.submittedAt()
        );
    }
}
