package com.agentstudy.learn.dto;

import com.agentstudy.learn.exercise.ExerciseJudgeResult;

public record ExerciseJudgeResultResponse(
        String questionId,
        String studentAnswer,
        String standardAnswer,
        boolean correct,
        String detail
) {

    public static ExerciseJudgeResultResponse from(ExerciseJudgeResult result) {
        return new ExerciseJudgeResultResponse(
                result.questionId(),
                result.studentAnswer(),
                result.standardAnswer(),
                result.correct(),
                result.detail()
        );
    }
}

