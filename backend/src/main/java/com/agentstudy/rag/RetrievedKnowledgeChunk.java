package com.agentstudy.rag;

public record RetrievedKnowledgeChunk(
        String id,
        String chapter,
        String title,
        String content,
        double score
) {
}

