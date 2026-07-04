package com.agentstudy.learn.dto;

import com.agentstudy.learn.LearningSessionStatus;
import com.agentstudy.learn.LearningState;
import java.time.Instant;

public record LearningSessionResponse(
        String sessionId,
        String studentName,
        int currentStep,
        LearningSessionStatus status,
        Instant createdAt,
        Instant updatedAt,
        String nextAction
) {

    public static LearningSessionResponse from(LearningState state) {
        return new LearningSessionResponse(
                state.getSessionId(),
                state.getStudentName(),
                state.getCurrentStep(),
                state.getStatus(),
                state.getCreatedAt(),
                state.getUpdatedAt(),
                nextActionOf(state.getCurrentStep(), state.getStatus())
        );
    }

    private static String nextActionOf(int currentStep, LearningSessionStatus status) {
        if (status == LearningSessionStatus.FINISHED) {
            return "finished";
        }
        return switch (currentStep) {
            case 1 -> "diagnosis";
            case 2 -> "plan";
            case 3 -> "lesson";
            case 4 -> "exercises";
            case 5 -> "review";
            default -> "unknown";
        };
    }
}

