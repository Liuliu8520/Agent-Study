package com.agentstudy.learn.dto;

import com.agentstudy.learn.plan.LearningPlan;

public record LearningPlanResponse(
        String sessionId,
        LearningPlan plan,
        int currentStep,
        String nextAction
) {
}

