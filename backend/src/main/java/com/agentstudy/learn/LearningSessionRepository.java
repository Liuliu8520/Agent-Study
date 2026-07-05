package com.agentstudy.learn;

import java.util.List;
import java.util.Optional;

public interface LearningSessionRepository {

    LearningState save(LearningState state);

    Optional<LearningState> findById(String sessionId);

    List<LearningState> findAll();

    int count();
}
