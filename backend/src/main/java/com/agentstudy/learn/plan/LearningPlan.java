package com.agentstudy.learn.plan;

import java.util.List;

public record LearningPlan(
        String title,
        List<LearningPlanDay> days
) {
}

