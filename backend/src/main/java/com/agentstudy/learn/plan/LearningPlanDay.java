package com.agentstudy.learn.plan;

import java.util.List;

public record LearningPlanDay(
        int day,
        String goal,
        List<String> concepts,
        List<String> practiceSuggestions
) {
}

