package com.agentstudy.learn.dto;

import java.util.List;

public record DiagnosisQuestionSetResponse(
        String sessionId,
        int currentStep,
        String nextAction,
        List<DiagnosisQuestionResponse> questions
) {
}

