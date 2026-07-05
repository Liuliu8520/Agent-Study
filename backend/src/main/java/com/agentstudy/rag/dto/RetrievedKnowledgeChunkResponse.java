package com.agentstudy.rag.dto;

import com.agentstudy.rag.RetrievedKnowledgeChunk;

public record RetrievedKnowledgeChunkResponse(
        String id,
        String chapter,
        String title,
        String content,
        double score
) {

    public static RetrievedKnowledgeChunkResponse from(RetrievedKnowledgeChunk chunk) {
        return new RetrievedKnowledgeChunkResponse(
                chunk.id(),
                chunk.chapter(),
                chunk.title(),
                chunk.content(),
                chunk.score()
        );
    }
}
