package com.agentstudy.agent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Profile("!dev")
public class InMemoryAgentCallLogRepository implements AgentCallLogRepository {

    private final ConcurrentMap<String, AgentCallLog> logs = new ConcurrentHashMap<>();

    @Override
    public AgentCallLog save(AgentCallLog log) {
        logs.put(log.callId(), log);
        return log;
    }

    @Override
    public Optional<AgentCallLog> findById(String callId) {
        return Optional.ofNullable(logs.get(callId));
    }

    @Override
    public List<AgentCallLog> findLatest(int limit) {
        return search(new AgentCallLogQuery(limit, null, null, null, null));
    }

    @Override
    public List<AgentCallLog> search(AgentCallLogQuery query) {
        return logs.values().stream()
                .filter(log -> matchesSessionId(log, query.sessionId()))
                .filter(log -> query.agentType() == null || log.agentType() == query.agentType())
                .filter(log -> query.status() == null || log.status() == query.status())
                .filter(log -> matchesPromptCode(log, query.promptCode()))
                .sorted(Comparator.comparing(AgentCallLog::createdAt).reversed())
                .limit(Math.max(0, query.limit()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private boolean matchesSessionId(AgentCallLog log, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return true;
        }
        return sessionId.trim().equals(log.sessionId());
    }

    private boolean matchesPromptCode(AgentCallLog log, String promptCode) {
        if (!StringUtils.hasText(promptCode)) {
            return true;
        }
        return promptCode.trim().equals(log.promptCode());
    }
}
