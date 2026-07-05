package com.agentstudy.learn;

import java.util.Optional;

public interface LearningSessionRepository {

    LearningState save(LearningState state);

    Optional<LearningState> findById(String sessionId);

    int count();
}
