package com.agentstudy.learn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitDiagnosisRequest(
        @NotEmpty(message = "Diagnosis answers are required")
        List<@Valid DiagnosisAnswerRequest> answers
) {
}

