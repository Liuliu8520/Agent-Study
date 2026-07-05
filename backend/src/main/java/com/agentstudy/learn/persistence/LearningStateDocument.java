package com.agentstudy.learn.persistence;

import com.agentstudy.learn.LearningSessionStatus;
import com.agentstudy.learn.LearningState;
import com.agentstudy.learn.diagnosis.DiagnosisQuestion;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import com.agentstudy.learn.exercise.ExerciseQuestion;
import com.agentstudy.learn.plan.LearningPlan;
import com.agentstudy.learn.review.ReviewResult;
import com.agentstudy.rag.RetrievedKnowledgeChunk;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LearningStateDocument(
        String sessionId,
        String studentName,
        Instant createdAt,
        int currentStep,
        LearningSessionStatus status,
        Instant updatedAt,
        List<DiagnosisQuestion> diagnosisQuestions,
        Map<String, String> diagnosisAnswers,
        List<WeakPoint> weakPoints,
        LearningPlan learningPlan,
        String generatedLesson,
        List<RetrievedKnowledgeChunk> retrievedChunks,
        List<ExerciseQuestion> exercises,
        List<ExerciseJudgeResult> exerciseResults,
        ReviewResult reviewResult
) {

    public static LearningStateDocument from(LearningState state) {
        return new LearningStateDocument(
                state.getSessionId(),
                state.getStudentName(),
                state.getCreatedAt(),
                state.getCurrentStep(),
                state.getStatus(),
                state.getUpdatedAt(),
                state.getDiagnosisQuestions(),
                state.getDiagnosisAnswers(),
                state.getWeakPoints(),
                state.getLearningPlan(),
                state.getGeneratedLesson(),
                state.getRetrievedChunks(),
                state.getExercises(),
                state.getExerciseResults(),
                state.getReviewResult()
        );
    }

    public LearningState toLearningState() {
        return LearningState.restore(
                sessionId,
                studentName,
                createdAt,
                currentStep,
                status,
                updatedAt,
                diagnosisQuestions,
                diagnosisAnswers,
                weakPoints,
                learningPlan,
                generatedLesson,
                retrievedChunks,
                exercises,
                exerciseResults,
                reviewResult
        );
    }
}
