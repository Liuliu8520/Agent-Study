package com.agentstudy.agent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

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
        return logs.values().stream()
                .sorted(Comparator.comparing(AgentCallLog::createdAt).reversed())
                .limit(Math.max(0, limit))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
