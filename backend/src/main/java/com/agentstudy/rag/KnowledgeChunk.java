package com.agentstudy.rag;

import java.util.List;

public record KnowledgeChunk(
        String id,
        String chapter,
        String title,
        String content,
        List<String> tags
) {
}

