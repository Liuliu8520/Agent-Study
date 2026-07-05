package com.agentstudy.learn;

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
public class InMemoryLearningSessionRepository implements LearningSessionRepository {

    private final ConcurrentMap<String, LearningState> sessions = new ConcurrentHashMap<>();

    @Override
    public LearningState save(LearningState state) {
        sessions.put(state.getSessionId(), state);
        return state;
    }

    @Override
    public Optional<LearningState> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<LearningState> findAll() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(LearningState::getUpdatedAt).reversed())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public int count() {
        return sessions.size();
    }
}
