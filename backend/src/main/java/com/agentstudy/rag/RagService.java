package com.agentstudy.rag;

import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.plan.LearningPlan;
import java.util.List;

public interface RagService {

    List<RetrievedKnowledgeChunk> retrieve(List<WeakPoint> weakPoints, LearningPlan plan);

    List<RetrievedKnowledgeChunk> retrieveByKeywords(List<String> keywords, int limit);
}
