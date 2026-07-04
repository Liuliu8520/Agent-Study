package com.agentstudy.learn;

import com.agentstudy.learn.diagnosis.DiagnosisQuestion;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import com.agentstudy.learn.exercise.ExerciseQuestion;
import com.agentstudy.learn.plan.LearningPlan;
import com.agentstudy.learn.review.ReviewResult;
import com.agentstudy.rag.RetrievedKnowledgeChunk;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LearningState {

    private final String sessionId;
    private final String studentName;
    private final Instant createdAt;
    private int currentStep;
    private LearningSessionStatus status;
    private Instant updatedAt;
    private List<DiagnosisQuestion> diagnosisQuestions;
    private Map<String, String> diagnosisAnswers;
    private List<WeakPoint> weakPoints;
    private LearningPlan learningPlan;
    private String generatedLesson;
    private List<RetrievedKnowledgeChunk> retrievedChunks;
    private List<ExerciseQuestion> exercises;
    private List<ExerciseJudgeResult> exerciseResults;
    private ReviewResult reviewResult;

    private LearningState(String sessionId, String studentName, Instant createdAt) {
        this.sessionId = sessionId;
        this.studentName = studentName;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.currentStep = 1;
        this.status = LearningSessionStatus.CREATED;
        this.weakPoints = new ArrayList<>();
        this.diagnosisQuestions = new ArrayList<>();
        this.diagnosisAnswers = new LinkedHashMap<>();
        this.retrievedChunks = new ArrayList<>();
        this.exercises = new ArrayList<>();
        this.exerciseResults = new ArrayList<>();
    }

    public static LearningState create(String sessionId, String studentName) {
        return new LearningState(sessionId, studentName, Instant.now());
    }

    public void markInProgress() {
        this.status = LearningSessionStatus.IN_PROGRESS;
        touch();
    }

    public void finish(ReviewResult reviewResult) {
        this.status = LearningSessionStatus.FINISHED;
        this.reviewResult = reviewResult;
        touch();
    }

    public void advanceToStep(int nextStep) {
        this.currentStep = nextStep;
        this.status = LearningSessionStatus.IN_PROGRESS;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public LearningSessionStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<DiagnosisQuestion> getDiagnosisQuestions() {
        return diagnosisQuestions;
    }

    public void setDiagnosisQuestions(List<DiagnosisQuestion> diagnosisQuestions) {
        this.diagnosisQuestions = diagnosisQuestions == null ? new ArrayList<>() : new ArrayList<>(diagnosisQuestions);
        touch();
    }

    public Map<String, String> getDiagnosisAnswers() {
        return diagnosisAnswers;
    }

    public void setDiagnosisAnswers(Map<String, String> diagnosisAnswers) {
        this.diagnosisAnswers = diagnosisAnswers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(diagnosisAnswers);
        touch();
    }

    public List<WeakPoint> getWeakPoints() {
        return weakPoints;
    }

    public void setWeakPoints(List<WeakPoint> weakPoints) {
        this.weakPoints = weakPoints == null ? new ArrayList<>() : new ArrayList<>(weakPoints);
        touch();
    }

    public LearningPlan getLearningPlan() {
        return learningPlan;
    }

    public void setLearningPlan(LearningPlan learningPlan) {
        this.learningPlan = learningPlan;
        touch();
    }

    public String getGeneratedLesson() {
        return generatedLesson;
    }

    public void setGeneratedLesson(String generatedLesson) {
        this.generatedLesson = generatedLesson;
        touch();
    }

    public List<RetrievedKnowledgeChunk> getRetrievedChunks() {
        return retrievedChunks;
    }

    public void setRetrievedChunks(List<RetrievedKnowledgeChunk> retrievedChunks) {
        this.retrievedChunks = retrievedChunks == null ? new ArrayList<>() : new ArrayList<>(retrievedChunks);
        touch();
    }

    public List<ExerciseQuestion> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseQuestion> exercises) {
        this.exercises = exercises == null ? new ArrayList<>() : new ArrayList<>(exercises);
        touch();
    }

    public List<ExerciseJudgeResult> getExerciseResults() {
        return exerciseResults;
    }

    public void setExerciseResults(List<ExerciseJudgeResult> exerciseResults) {
        this.exerciseResults = exerciseResults == null ? new ArrayList<>() : new ArrayList<>(exerciseResults);
        touch();
    }

    public ReviewResult getReviewResult() {
        return reviewResult;
    }
}
