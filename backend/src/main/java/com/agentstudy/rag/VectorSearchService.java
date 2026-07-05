package com.agentstudy.rag;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class VectorSearchService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EmbeddingClient embeddingClient;

    public VectorSearchService(KnowledgeChunkRepository knowledgeChunkRepository, EmbeddingClient embeddingClient) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.embeddingClient = embeddingClient;
    }

    public List<RetrievedKnowledgeChunk> search(String queryText, List<String> keywords, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        Set<String> keywordSet = keywords == null ? Set.of() : new LinkedHashSet<>(keywords);
        List<Double> queryEmbedding = embeddingClient.embed(buildQueryText(queryText, keywordSet));

        List<RetrievedKnowledgeChunk> retrieved = knowledgeChunkRepository.findAll().stream()
                .map(chunk -> toRetrievedChunk(chunk, score(queryEmbedding, keywordSet, chunk)))
                .filter(chunk -> chunk.score() > 0)
                .sorted(Comparator.comparingDouble(RetrievedKnowledgeChunk::score).reversed())
                .limit(safeLimit)
                .toList();

        if (!retrieved.isEmpty()) {
            return retrieved;
        }

        return knowledgeChunkRepository.findAll().stream()
                .limit(Math.min(safeLimit, 3))
                .map(chunk -> toRetrievedChunk(chunk, 1.0))
                .toList();
    }

    public List<Double> embedChunk(KnowledgeChunk chunk) {
        return embeddingClient.embed(chunk.searchableText());
    }

    private double score(List<Double> queryEmbedding, Set<String> keywords, KnowledgeChunk chunk) {
        List<Double> chunkEmbedding = chunk.hasEmbedding() ? chunk.embedding() : embedChunk(chunk);
        return cosine(queryEmbedding, chunkEmbedding) + keywordBoost(chunk, keywords);
    }

    private double keywordBoost(KnowledgeChunk chunk, Set<String> keywords) {
        double score = 0;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
            if (chunk.tags().stream().anyMatch(tag -> tag.equalsIgnoreCase(normalizedKeyword) || tag.equals(keyword))) {
                score += 0.8;
            }
            if (chunk.title().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                score += 0.4;
            }
            if (chunk.content().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                score += 0.2;
            }
        }
        return score;
    }

    private double cosine(List<Double> left, List<Double> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        int size = Math.min(left.size(), right.size());
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int index = 0; index < size; index++) {
            double leftValue = left.get(index);
            double rightValue = right.get(index);
            dot += leftValue * rightValue;
            leftNorm += leftValue * leftValue;
            rightNorm += rightValue * rightValue;
        }
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private RetrievedKnowledgeChunk toRetrievedChunk(KnowledgeChunk chunk, double score) {
        return new RetrievedKnowledgeChunk(chunk.id(), chunk.chapter(), chunk.title(), chunk.content(), score);
    }

    private String buildQueryText(String queryText, Set<String> keywords) {
        StringBuilder builder = new StringBuilder(queryText == null ? "" : queryText);
        keywords.forEach(keyword -> builder.append(' ').append(keyword));
        return builder.toString();
    }
}
