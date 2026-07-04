package com.agentstudy.learn.dto;

import jakarta.validation.constraints.Size;

public record CreateLearningSessionRequest(
        @Size(max = 50, message = "Student name must be 50 characters or less")
        String studentName
) {
}

