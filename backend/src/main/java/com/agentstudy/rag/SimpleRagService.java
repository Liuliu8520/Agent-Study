package com.agentstudy.rag;

import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.plan.LearningPlan;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SimpleRagService {

    private final InMemoryKnowledgeBase knowledgeBase;

    public SimpleRagService(InMemoryKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public List<RetrievedKnowledgeChunk> retrieve(List<WeakPoint> weakPoints, LearningPlan plan) {
        Set<String> keywords = buildKeywords(weakPoints, plan);
        List<RetrievedKnowledgeChunk> retrieved = knowledgeBase.listChunks().stream()
                .map(chunk -> toRetrievedChunk(chunk, score(chunk, keywords)))
                .filter(chunk -> chunk.score() > 0)
                .sorted(Comparator.comparingDouble(RetrievedKnowledgeChunk::score).reversed())
                .limit(4)
                .toList();

        if (!retrieved.isEmpty()) {
            return retrieved;
        }

        return knowledgeBase.listChunks().stream()
                .limit(3)
                .map(chunk -> toRetrievedChunk(chunk, 1.0))
                .toList();
    }

    private Set<String> buildKeywords(List<WeakPoint> weakPoints, LearningPlan plan) {
        Set<String> keywords = new LinkedHashSet<>();
        if (weakPoints != null) {
            weakPoints.forEach(weakPoint -> {
                keywords.add(weakPoint.code());
                keywords.add(weakPoint.name());
            });
        }
        if (plan != null) {
            plan.days().forEach(day -> keywords.addAll(day.concepts()));
        }
        return keywords;
    }

    private double score(KnowledgeChunk chunk, Set<String> keywords) {
        double score = 0;
        for (String keyword : keywords) {
            String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
            if (chunk.tags().stream().anyMatch(tag -> tag.equalsIgnoreCase(normalizedKeyword) || tag.equals(keyword))) {
                score += 3;
            }
            if (chunk.title().contains(keyword)) {
                score += 2;
            }
            if (chunk.content().contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }

    private RetrievedKnowledgeChunk toRetrievedChunk(KnowledgeChunk chunk, double score) {
        return new RetrievedKnowledgeChunk(chunk.id(), chunk.chapter(), chunk.title(), chunk.content(), score);
    }
}

