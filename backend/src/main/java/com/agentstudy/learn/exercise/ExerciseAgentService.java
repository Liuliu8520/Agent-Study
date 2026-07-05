package com.agentstudy.learn.exercise;

import com.agentstudy.agent.AgentRunCommand;
import com.agentstudy.agent.AgentRunResult;
import com.agentstudy.agent.AgentRuntimeService;
import com.agentstudy.learn.diagnosis.WeakPoint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExerciseAgentService {

    private static final String PROMPT_CODE = "exercise.generate";

    private final AgentRuntimeService agentRuntimeService;

    public ExerciseAgentService(AgentRuntimeService agentRuntimeService) {
        this.agentRuntimeService = agentRuntimeService;
    }

    public AgentRunResult generateDraft(String sessionId, List<WeakPoint> weakPoints, String lessonMarkdown) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("lessonMarkdown", lessonMarkdown);
        variables.put("knowledgePoints", weakPoints.stream()
                .map(weakPoint -> weakPoint.code() + "/" + weakPoint.name())
                .collect(Collectors.joining(", ")));
        return agentRuntimeService.run(new AgentRunCommand(sessionId, PROMPT_CODE, variables));
    }
}
