package com.agentstudy.learn.dto;

import com.agentstudy.learn.review.ReviewStatus;
import java.util.List;

public record ReviewResponse(
        String sessionId,
        ReviewStatus status,
        String message,
        double errorRate,
        List<String> suggestions,
        List<VariantExerciseResponse> variantExercises,
        int currentStep,
        String nextAction,
        boolean finished
) {
}

