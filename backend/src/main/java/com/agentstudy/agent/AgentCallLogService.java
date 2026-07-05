package com.agentstudy.agent;

import com.agentstudy.common.BusinessException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AgentCallLogService {

    private final AgentCallLogRepository repository;

    public AgentCallLogService(AgentCallLogRepository repository) {
        this.repository = repository;
    }

    public AgentCallLog recordSuccess(LlmRequest request, LlmResponse response, Duration duration) {
        return repository.save(new AgentCallLog(
                UUID.randomUUID().toString(),
                request.sessionId(),
                request.agentType(),
                request.promptCode(),
                request.promptVersion(),
                response.modelName(),
                buildRequestPayload(request),
                response.content(),
                AgentCallStatus.SUCCESS,
                null,
                duration.toMillis(),
                Instant.now()
        ));
    }

    public AgentCallLog recordFailure(LlmRequest request, Exception exception, Duration duration) {
        return repository.save(new AgentCallLog(
                UUID.randomUUID().toString(),
                request.sessionId(),
                request.agentType(),
                request.promptCode(),
                request.promptVersion(),
                "unknown",
                buildRequestPayload(request),
                null,
                AgentCallStatus.FAILED,
                exception.getMessage(),
                duration.toMillis(),
                Instant.now()
        ));
    }

    public List<AgentCallLog> findLatest(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return repository.findLatest(safeLimit);
    }

    public AgentCallLog getRequired(String callId) {
        return repository.findById(callId)
                .orElseThrow(() -> BusinessException.notFound("Agent call log not found: " + callId));
    }

    private String buildRequestPayload(LlmRequest request) {
        return """
                system:
                %s

                user:
                %s
                """.formatted(request.systemPrompt(), request.userPrompt());
    }
}
