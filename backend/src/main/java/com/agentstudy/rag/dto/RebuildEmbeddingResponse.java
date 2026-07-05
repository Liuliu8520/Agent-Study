package com.agentstudy.rag.dto;

public record RebuildEmbeddingResponse(
        String chunkId,
        boolean embeddingReady,
        int embeddingDimensions
) {
}
