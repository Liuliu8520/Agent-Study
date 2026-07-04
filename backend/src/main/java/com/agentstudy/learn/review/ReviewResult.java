package com.agentstudy.learn.review;

import java.util.List;

public record ReviewResult(
        ReviewStatus status,
        String message,
        double errorRate,
        List<String> suggestions,
        List<VariantExercise> variantExercises
) {
}

