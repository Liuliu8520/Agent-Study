package com.agentstudy.learn.dto;

import com.agentstudy.learn.exercise.ExerciseQuestion;

public record ExerciseQuestionResponse(
        String id,
        String stem,
        String knowledgePoint
) {

    public static ExerciseQuestionResponse from(ExerciseQuestion question) {
        return new ExerciseQuestionResponse(question.id(), question.stem(), question.knowledgePoint());
    }
}

