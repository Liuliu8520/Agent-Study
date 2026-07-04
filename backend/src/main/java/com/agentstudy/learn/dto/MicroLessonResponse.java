package com.agentstudy.learn.dto;

import java.util.List;

public record MicroLessonResponse(
        String sessionId,
        String lessonMarkdown,
        List<RetrievedChunkResponse> retrievedChunks,
        int currentStep,
        String nextAction
) {
}

