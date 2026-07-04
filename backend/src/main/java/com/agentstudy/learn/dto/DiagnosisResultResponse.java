package com.agentstudy.learn.dto;

import com.agentstudy.learn.diagnosis.WeakPoint;
import java.util.List;

public record DiagnosisResultResponse(
        String sessionId,
        int correctCount,
        int totalCount,
        List<WeakPoint> weakPoints,
        int currentStep,
        String nextAction
) {
}

