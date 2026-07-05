package com.agentstudy.rag.dto;

import com.agentstudy.rag.KnowledgeChunk;
import java.util.List;

public record KnowledgeChunkResponse(
        String id,
        String chapter,
        String title,
        String content,
        List<String> tags,
        boolean embeddingReady,
        int embeddingDimensions
) {

    public static KnowledgeChunkResponse from(KnowledgeChunk chunk) {
        return new KnowledgeChunkResponse(
                chunk.id(),
                chunk.chapter(),
                chunk.title(),
                chunk.content(),
                chunk.tags(),
                chunk.hasEmbedding(),
                chunk.embedding().size()
        );
    }
}
