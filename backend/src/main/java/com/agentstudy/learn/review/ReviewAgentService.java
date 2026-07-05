package com.agentstudy.learn.review;

import com.agentstudy.agent.AgentRunCommand;
import com.agentstudy.agent.AgentRunResult;
import com.agentstudy.agent.AgentRuntimeService;
import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReviewAgentService {

    private static final String PROMPT_CODE = "review.feedback";

    private final AgentRuntimeService agentRuntimeService;

    public ReviewAgentService(AgentRuntimeService agentRuntimeService) {
        this.agentRuntimeService = agentRuntimeService;
    }

    public AgentRunResult generateDraft(String sessionId, List<ExerciseJudgeResult> exerciseResults) {
        List<ExerciseJudgeResult> safeResults = exerciseResults == null ? List.of() : exerciseResults;
        long wrongCount = safeResults.stream().filter(result -> !result.correct()).count();
        double errorRate = safeResults.isEmpty() ? 0 : (double) wrongCount / safeResults.size();

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("exerciseResults", safeResults.toString());
        variables.put("errorRate", errorRate);
        return agentRuntimeService.run(new AgentRunCommand(sessionId, PROMPT_CODE, variables));
    }
}
