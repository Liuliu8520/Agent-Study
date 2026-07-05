package com.agentstudy.rag.dto;

import com.agentstudy.rag.KnowledgeChunk;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record UpsertKnowledgeChunkRequest(
        String id,
        @NotBlank String chapter,
        @NotBlank String title,
        @NotBlank String content,
        List<String> tags
) {

    public KnowledgeChunk toChunk(String pathId) {
        String chunkId = pathId == null || pathId.isBlank()
                ? (id == null || id.isBlank() ? UUID.randomUUID().toString() : id.trim())
                : pathId.trim();
        return new KnowledgeChunk(
                chunkId,
                chapter.trim(),
                title.trim(),
                content.trim(),
                tags == null ? List.of() : tags.stream()
                        .filter(tag -> tag != null && !tag.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList()
        );
    }
}
