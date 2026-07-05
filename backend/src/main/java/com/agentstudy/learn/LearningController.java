package com.agentstudy.learn;

import com.agentstudy.common.ApiResponse;
import com.agentstudy.learn.dto.CreateLearningSessionRequest;
import com.agentstudy.learn.dto.DiagnosisQuestionSetResponse;
import com.agentstudy.learn.dto.DiagnosisResultResponse;
import com.agentstudy.learn.dto.ExerciseAttemptResponse;
import com.agentstudy.learn.dto.ExerciseQuestionSetResponse;
import com.agentstudy.learn.dto.ExerciseSubmitResponse;
import com.agentstudy.learn.dto.LearningPlanResponse;
import com.agentstudy.learn.dto.LearningSessionResponse;
import com.agentstudy.learn.dto.MicroLessonResponse;
import com.agentstudy.learn.dto.ReviewResponse;
import com.agentstudy.learn.dto.SubmitExerciseRequest;
import com.agentstudy.learn.dto.SubmitDiagnosisRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learn/sessions")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @PostMapping
    public ApiResponse<LearningSessionResponse> createSession(
            @Valid @RequestBody(required = false) CreateLearningSessionRequest request
    ) {
        return ApiResponse.success(learningService.createSession(request));
    }

    @GetMapping
    public ApiResponse<List<LearningSessionResponse>> listSessions(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) LearningSessionStatus status,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(learningService.listSessions(studentName, status, limit));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<LearningSessionResponse> getSession(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.getSession(sessionId));
    }

    @PostMapping("/{sessionId}/diagnosis/questions")
    public ApiResponse<DiagnosisQuestionSetResponse> generateDiagnosisQuestions(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.generateDiagnosisQuestions(sessionId));
    }

    @PostMapping("/{sessionId}/diagnosis/submit")
    public ApiResponse<DiagnosisResultResponse> submitDiagnosis(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitDiagnosisRequest request
    ) {
        return ApiResponse.success(learningService.submitDiagnosis(sessionId, request));
    }

    @PostMapping("/{sessionId}/plan")
    public ApiResponse<LearningPlanResponse> generateLearningPlan(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.generateLearningPlan(sessionId));
    }

    @PostMapping("/{sessionId}/lesson")
    public ApiResponse<MicroLessonResponse> generateMicroLesson(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.generateMicroLesson(sessionId));
    }

    @PostMapping("/{sessionId}/exercises")
    public ApiResponse<ExerciseQuestionSetResponse> generateExercises(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.generateExercises(sessionId));
    }

    @PostMapping("/{sessionId}/exercises/submit")
    public ApiResponse<ExerciseSubmitResponse> submitExercises(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitExerciseRequest request
    ) {
        return ApiResponse.success(learningService.submitExercises(sessionId, request));
    }

    @GetMapping("/{sessionId}/exercise-attempts")
    public ApiResponse<List<ExerciseAttemptResponse>> listExerciseAttempts(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.listExerciseAttempts(sessionId));
    }

    @PostMapping("/{sessionId}/review")
    public ApiResponse<ReviewResponse> generateReview(@PathVariable String sessionId) {
        return ApiResponse.success(learningService.generateReview(sessionId));
    }
}
