package com.agentstudy.learn.plan;

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
public class LearningPlanAgentService {

    private static final String PROMPT_CODE = "planner.three-day";

    private final AgentRuntimeService agentRuntimeService;

    public LearningPlanAgentService(AgentRuntimeService agentRuntimeService) {
        this.agentRuntimeService = agentRuntimeService;
    }

    public AgentRunResult generateDraft(String sessionId, List<WeakPoint> weakPoints) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("weakPoints", weakPoints.stream()
                .map(weakPoint -> weakPoint.code() + "/" + weakPoint.name() + ":" + weakPoint.reason())
                .collect(Collectors.joining("; ")));
        variables.put("dailyMinutes", 45);
        return agentRuntimeService.run(new AgentRunCommand(sessionId, PROMPT_CODE, variables));
    }
}
