package com.agentstudy.learn.dto;

import com.agentstudy.learn.review.VariantExercise;

public record VariantExerciseResponse(
        String id,
        String stem,
        String knowledgePoint
) {

    public static VariantExerciseResponse from(VariantExercise exercise) {
        return new VariantExerciseResponse(exercise.id(), exercise.stem(), exercise.knowledgePoint());
    }
}

