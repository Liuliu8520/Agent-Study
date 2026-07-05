package com.agentstudy.learn.exercise;

import java.util.List;

public interface ExerciseAttemptRepository {

    ExerciseAttempt save(ExerciseAttempt attempt);

    List<ExerciseAttempt> findBySessionId(String sessionId);
}
