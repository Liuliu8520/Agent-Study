package com.agentstudy.learn.exercise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class InMemoryExerciseAttemptRepository implements ExerciseAttemptRepository {

    private final ConcurrentMap<String, ExerciseAttempt> attempts = new ConcurrentHashMap<>();

    @Override
    public ExerciseAttempt save(ExerciseAttempt attempt) {
        attempts.put(attempt.attemptId(), attempt);
        return attempt;
    }

    @Override
    public List<ExerciseAttempt> findBySessionId(String sessionId) {
        return attempts.values().stream()
                .filter(attempt -> attempt.sessionId().equals(sessionId))
                .sorted(Comparator.comparing(ExerciseAttempt::submittedAt).reversed())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
