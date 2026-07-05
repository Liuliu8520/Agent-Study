package com.agentstudy.learn.lesson;

import com.agentstudy.agent.AgentRunCommand;
import com.agentstudy.agent.AgentRunResult;
import com.agentstudy.agent.AgentRuntimeService;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.plan.LearningPlan;
import com.agentstudy.rag.RetrievedKnowledgeChunk;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MicroLessonAgentService {

    private static final String PROMPT_CODE = "lesson.micro";

    private final AgentRuntimeService agentRuntimeService;

    public MicroLessonAgentService(AgentRuntimeService agentRuntimeService) {
        this.agentRuntimeService = agentRuntimeService;
    }

    public AgentRunResult generateDraft(
            String sessionId,
            LearningPlan plan,
            List<WeakPoint> weakPoints,
            List<RetrievedKnowledgeChunk> chunks
    ) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("learningPlan", plan.title());
        variables.put("weakPoints", weakPoints.stream()
                .map(weakPoint -> weakPoint.code() + "/" + weakPoint.name())
                .collect(Collectors.joining(", ")));
        variables.put("retrievedChunks", chunks.stream()
                .map(chunk -> chunk.id() + ":" + chunk.title())
                .collect(Collectors.joining("; ")));
        return agentRuntimeService.run(new AgentRunCommand(sessionId, PROMPT_CODE, variables));
    }
}
