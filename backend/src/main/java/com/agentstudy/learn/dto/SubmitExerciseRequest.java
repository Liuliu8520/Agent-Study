package com.agentstudy.learn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitExerciseRequest(
        @NotEmpty(message = "Exercise answers are required")
        List<@Valid ExerciseAnswerRequest> answers
) {
}

