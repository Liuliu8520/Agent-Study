package com.agentstudy.rag;

import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.plan.LearningPlan;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SimpleRagService implements RagService {

    private final VectorSearchService vectorSearchService;

    public SimpleRagService(VectorSearchService vectorSearchService) {
        this.vectorSearchService = vectorSearchService;
    }

    @Override
    public List<RetrievedKnowledgeChunk> retrieve(List<WeakPoint> weakPoints, LearningPlan plan) {
        Set<String> keywords = buildKeywords(weakPoints, plan);
        return vectorSearchService.search(String.join(" ", keywords), List.copyOf(keywords), 4);
    }

    @Override
    public List<RetrievedKnowledgeChunk> retrieveByKeywords(List<String> keywords, int limit) {
        Set<String> keywordSet = keywords == null ? Set.of() : new LinkedHashSet<>(keywords);
        return vectorSearchService.search(String.join(" ", keywordSet), List.copyOf(keywordSet), limit);
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
}
