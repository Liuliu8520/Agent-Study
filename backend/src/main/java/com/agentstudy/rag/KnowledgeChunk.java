package com.agentstudy.rag;

import java.util.List;
import java.util.StringJoiner;

public record KnowledgeChunk(
        String id,
        String chapter,
        String title,
        String content,
        List<String> tags,
        List<Double> embedding
) {

    public KnowledgeChunk {
        tags = tags == null ? List.of() : List.copyOf(tags);
        embedding = embedding == null ? List.of() : List.copyOf(embedding);
    }

    public KnowledgeChunk(
            String id,
            String chapter,
            String title,
            String content,
            List<String> tags
    ) {
        this(id, chapter, title, content, tags, List.of());
    }

    public KnowledgeChunk withEmbedding(List<Double> embedding) {
        return new KnowledgeChunk(id, chapter, title, content, tags, embedding);
    }

    public String searchableText() {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(chapter);
        joiner.add(title);
        joiner.add(content);
        tags.forEach(joiner::add);
        return joiner.toString();
    }

    public boolean hasEmbedding() {
        return !embedding.isEmpty();
    }
}
