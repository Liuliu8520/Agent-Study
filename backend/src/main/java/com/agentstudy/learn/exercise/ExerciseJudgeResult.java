package com.agentstudy.learn.exercise;

public record ExerciseJudgeResult(
        String questionId,
        String studentAnswer,
        String standardAnswer,
        boolean correct,
        String detail
) {
}

