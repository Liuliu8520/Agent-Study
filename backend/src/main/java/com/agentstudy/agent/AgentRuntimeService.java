package com.agentstudy.agent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AgentRuntimeService {

    private final PromptService promptService;
    private final LlmClient llmClient;
    private final AgentCallLogService callLogService;

    public AgentRuntimeService(PromptService promptService, LlmClient llmClient, AgentCallLogService callLogService) {
        this.promptService = promptService;
        this.llmClient = llmClient;
        this.callLogService = callLogService;
    }

    public AgentRunResult run(AgentRunCommand command) {
        RenderedPrompt prompt = promptService.render(command.promptCode(), command.variables());
        LlmRequest request = new LlmRequest(
                command.sessionId(),
                prompt.agentType(),
                prompt.code(),
                prompt.version(),
                prompt.systemPrompt(),
                prompt.userPrompt(),
                command.variables() == null ? Map.of() : command.variables()
        );

        Instant startedAt = Instant.now();
        try {
            LlmResponse response = llmClient.complete(request);
            AgentCallLog log = callLogService.recordSuccess(request, response, Duration.between(startedAt, Instant.now()));
            return AgentRunResult.from(log);
        } catch (RuntimeException exception) {
            callLogService.recordFailure(request, exception, Duration.between(startedAt, Instant.now()));
            throw exception;
        }
    }
}
