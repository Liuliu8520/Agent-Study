package com.agentstudy.agent;

import java.util.List;
import java.util.Optional;

public interface AgentCallLogRepository {

    AgentCallLog save(AgentCallLog log);

    Optional<AgentCallLog> findById(String callId);

    List<AgentCallLog> findLatest(int limit);

    List<AgentCallLog> search(AgentCallLogQuery query);
}
