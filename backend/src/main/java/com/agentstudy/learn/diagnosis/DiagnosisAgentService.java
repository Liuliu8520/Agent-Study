package com.agentstudy.learn.diagnosis;

import com.agentstudy.agent.AgentRunCommand;
import com.agentstudy.agent.AgentRunResult;
import com.agentstudy.agent.AgentRuntimeService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisAgentService {

    private static final String PROMPT_CODE = "diagnosis.default";

    private final AgentRuntimeService agentRuntimeService;

    public DiagnosisAgentService(AgentRuntimeService agentRuntimeService) {
        this.agentRuntimeService = agentRuntimeService;
    }

    public AgentRunResult analyzeResult(
            String sessionId,
            String studentName,
            int correctCount,
            int totalCount,
            Map<String, String> submittedAnswers,
            List<WeakPoint> weakPoints
    ) {
        List<WeakPoint> safeWeakPoints = weakPoints == null ? List.of() : weakPoints;
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("studentName", studentName);
        variables.put("diagnosisResult", buildDiagnosisResult(correctCount, totalCount, submittedAnswers, safeWeakPoints));
        return agentRuntimeService.run(new AgentRunCommand(sessionId, PROMPT_CODE, variables));
    }

    private String buildDiagnosisResult(
            int correctCount,
            int totalCount,
            Map<String, String> submittedAnswers,
            List<WeakPoint> weakPoints
    ) {
        String weakPointSummary = weakPoints.isEmpty()
                ? "none"
                : weakPoints.stream()
                        .map(weakPoint -> weakPoint.code() + "/" + weakPoint.name() + ":" + weakPoint.reason())
                        .collect(Collectors.joining("; "));
        return "correct=" + correctCount
                + ", total=" + totalCount
                + ", answers=" + submittedAnswers
                + ", weakPoints=" + weakPointSummary;
    }
}
