package com.agentstudy.learn;

import com.agentstudy.learn.dto.CreateLearningSessionRequest;
import com.agentstudy.learn.dto.DiagnosisQuestionSetResponse;
import com.agentstudy.learn.dto.DiagnosisResultResponse;
import com.agentstudy.learn.dto.ExerciseQuestionSetResponse;
import com.agentstudy.learn.dto.ExerciseSubmitResponse;
import com.agentstudy.learn.dto.LearningPlanResponse;
import com.agentstudy.learn.dto.LearningSessionResponse;
import com.agentstudy.learn.dto.MicroLessonResponse;
import com.agentstudy.learn.dto.ReviewResponse;
import com.agentstudy.learn.dto.SubmitExerciseRequest;
import com.agentstudy.learn.dto.SubmitDiagnosisRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LearningService {

    private static final String DEFAULT_STUDENT_NAME = "anonymous";

    private final LearningOrchestrator learningOrchestrator;

    public LearningService(LearningOrchestrator learningOrchestrator) {
        this.learningOrchestrator = learningOrchestrator;
    }

    public LearningSessionResponse createSession(CreateLearningSessionRequest request) {
        String studentName = normalizeStudentName(request);
        return learningOrchestrator.createSession(studentName);
    }

    public LearningSessionResponse getSession(String sessionId) {
        return learningOrchestrator.getSession(sessionId);
    }

    public DiagnosisQuestionSetResponse generateDiagnosisQuestions(String sessionId) {
        return learningOrchestrator.generateDiagnosisQuestions(sessionId);
    }

    public DiagnosisResultResponse submitDiagnosis(String sessionId, SubmitDiagnosisRequest request) {
        return learningOrchestrator.submitDiagnosis(sessionId, request);
    }

    public LearningPlanResponse generateLearningPlan(String sessionId) {
        return learningOrchestrator.generateLearningPlan(sessionId);
    }

    public MicroLessonResponse generateMicroLesson(String sessionId) {
        return learningOrchestrator.generateMicroLesson(sessionId);
    }

    public ExerciseQuestionSetResponse generateExercises(String sessionId) {
        return learningOrchestrator.generateExercises(sessionId);
    }

    public ExerciseSubmitResponse submitExercises(String sessionId, SubmitExerciseRequest request) {
        return learningOrchestrator.submitExercises(sessionId, request);
    }

    public ReviewResponse generateReview(String sessionId) {
        return learningOrchestrator.generateReview(sessionId);
    }

    private String normalizeStudentName(CreateLearningSessionRequest request) {
        if (request == null || !StringUtils.hasText(request.studentName())) {
            return DEFAULT_STUDENT_NAME;
        }
        return request.studentName().trim();
    }
}
