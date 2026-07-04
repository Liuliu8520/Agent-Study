package com.agentstudy.learn.dto;

import com.agentstudy.rag.RetrievedKnowledgeChunk;

public record RetrievedChunkResponse(
        String id,
        String chapter,
        String title,
        String content,
        double score
) {

    public static RetrievedChunkResponse from(RetrievedKnowledgeChunk chunk) {
        return new RetrievedChunkResponse(
                chunk.id(),
                chunk.chapter(),
                chunk.title(),
                chunk.content(),
                chunk.score()
        );
    }
}

