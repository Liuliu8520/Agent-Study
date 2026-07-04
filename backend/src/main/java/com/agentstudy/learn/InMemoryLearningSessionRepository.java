package com.agentstudy.learn;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLearningSessionRepository {

    private final ConcurrentMap<String, LearningState> sessions = new ConcurrentHashMap<>();

    public LearningState save(LearningState state) {
        sessions.put(state.getSessionId(), state);
        return state;
    }

    public Optional<LearningState> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public int count() {
        return sessions.size();
    }
}

