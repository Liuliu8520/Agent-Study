package com.agentstudy.learn;

import com.agentstudy.common.BusinessException;
import com.agentstudy.learn.diagnosis.DiagnosisQuestion;
import com.agentstudy.learn.diagnosis.DiagnosisQuestionBank;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.learn.dto.ExerciseAnswerRequest;
import com.agentstudy.learn.dto.ExerciseJudgeResultResponse;
import com.agentstudy.learn.dto.ExerciseQuestionResponse;
import com.agentstudy.learn.dto.ExerciseQuestionSetResponse;
import com.agentstudy.learn.dto.ExerciseSubmitResponse;
import com.agentstudy.learn.dto.DiagnosisAnswerRequest;
import com.agentstudy.learn.dto.DiagnosisQuestionResponse;
import com.agentstudy.learn.dto.DiagnosisQuestionSetResponse;
import com.agentstudy.learn.dto.DiagnosisResultResponse;
import com.agentstudy.learn.dto.LearningPlanResponse;
import com.agentstudy.learn.dto.LearningSessionResponse;
import com.agentstudy.learn.dto.MicroLessonResponse;
import com.agentstudy.learn.dto.RetrievedChunkResponse;
import com.agentstudy.learn.dto.ReviewResponse;
import com.agentstudy.learn.dto.SubmitExerciseRequest;
import com.agentstudy.learn.dto.SubmitDiagnosisRequest;
import com.agentstudy.learn.dto.VariantExerciseResponse;
import com.agentstudy.learn.plan.LearningPlan;
import com.agentstudy.learn.plan.LearningPlanGenerator;
import com.agentstudy.learn.lesson.MicroLessonGenerator;
import com.agentstudy.learn.exercise.ExerciseGenerator;
import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import com.agentstudy.learn.exercise.ExerciseQuestion;
import com.agentstudy.learn.exercise.ExpressionJudgeService;
import com.agentstudy.learn.review.ReviewGenerator;
import com.agentstudy.learn.review.ReviewResult;
import com.agentstudy.rag.RetrievedKnowledgeChunk;
import com.agentstudy.rag.RagService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LearningOrchestrator {

    private final LearningSessionRepository repository;
    private final DiagnosisQuestionBank diagnosisQuestionBank;
    private final LearningPlanGenerator learningPlanGenerator;
    private final RagService ragService;
    private final MicroLessonGenerator microLessonGenerator;
    private final ExerciseGenerator exerciseGenerator;
    private final ExpressionJudgeService expressionJudgeService;
    private final ReviewGenerator reviewGenerator;

    public LearningOrchestrator(
            LearningSessionRepository repository,
            DiagnosisQuestionBank diagnosisQuestionBank,
            LearningPlanGenerator learningPlanGenerator,
            RagService ragService,
            MicroLessonGenerator microLessonGenerator,
            ExerciseGenerator exerciseGenerator,
            ExpressionJudgeService expressionJudgeService,
            ReviewGenerator reviewGenerator
    ) {
        this.repository = repository;
        this.diagnosisQuestionBank = diagnosisQuestionBank;
        this.learningPlanGenerator = learningPlanGenerator;
        this.ragService = ragService;
        this.microLessonGenerator = microLessonGenerator;
        this.exerciseGenerator = exerciseGenerator;
        this.expressionJudgeService = expressionJudgeService;
        this.reviewGenerator = reviewGenerator;
    }

    public LearningSessionResponse createSession(String studentName) {
        LearningState state = LearningState.create(UUID.randomUUID().toString(), studentName);
        repository.save(state);
        return LearningSessionResponse.from(state);
    }

    public LearningSessionResponse getSession(String sessionId) {
        return LearningSessionResponse.from(getRequiredState(sessionId));
    }

    public DiagnosisQuestionSetResponse generateDiagnosisQuestions(String sessionId) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 1, "Diagnosis questions can only be generated at step 1");

        List<DiagnosisQuestion> questions = diagnosisQuestionBank.getDefaultQuestions();
        state.setDiagnosisQuestions(questions);
        state.markInProgress();
        repository.save(state);

        List<DiagnosisQuestionResponse> questionResponses = questions.stream()
                .map(DiagnosisQuestionResponse::from)
                .toList();
        return new DiagnosisQuestionSetResponse(state.getSessionId(), state.getCurrentStep(), "submitDiagnosis", questionResponses);
    }

    public DiagnosisResultResponse submitDiagnosis(String sessionId, SubmitDiagnosisRequest request) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 1, "Diagnosis answers can only be submitted at step 1");

        if (state.getDiagnosisQuestions().isEmpty()) {
            throw BusinessException.badRequest("Diagnosis questions have not been generated");
        }

        Map<String, String> submittedAnswers = toSubmittedAnswerMap(request);

        int correctCount = 0;
        List<WeakPoint> weakPoints = new java.util.ArrayList<>();
        for (DiagnosisQuestion question : state.getDiagnosisQuestions()) {
            String selectedOption = submittedAnswers.get(question.id());
            if (selectedOption == null) {
                throw BusinessException.badRequest("Missing answer for question: " + question.id());
            }
            ensureOptionExists(question, selectedOption);
            if (question.isCorrect(selectedOption)) {
                correctCount++;
            } else {
                weakPoints.add(question.weakPoint());
            }
        }

        state.setDiagnosisAnswers(submittedAnswers);
        state.setWeakPoints(weakPoints);
        state.advanceToStep(2);
        repository.save(state);

        return new DiagnosisResultResponse(
                state.getSessionId(),
                correctCount,
                state.getDiagnosisQuestions().size(),
                weakPoints,
                state.getCurrentStep(),
                "plan"
        );
    }

    public LearningPlanResponse generateLearningPlan(String sessionId) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 2, "Learning plan can only be generated after diagnosis");

        LearningPlan plan = learningPlanGenerator.generate(state.getSessionId(), state.getWeakPoints());
        state.setLearningPlan(plan);
        state.advanceToStep(3);
        repository.save(state);

        return new LearningPlanResponse(state.getSessionId(), plan, state.getCurrentStep(), "lesson");
    }

    public MicroLessonResponse generateMicroLesson(String sessionId) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 3, "Micro lesson can only be generated after learning plan");

        if (state.getLearningPlan() == null) {
            throw BusinessException.badRequest("Learning plan has not been generated");
        }

        List<RetrievedKnowledgeChunk> retrievedChunks = ragService.retrieve(state.getWeakPoints(), state.getLearningPlan());
        String lessonMarkdown = microLessonGenerator.generate(
                state.getSessionId(),
                state.getLearningPlan(),
                state.getWeakPoints(),
                retrievedChunks
        );
        state.setRetrievedChunks(retrievedChunks);
        state.setGeneratedLesson(lessonMarkdown);
        state.advanceToStep(4);
        repository.save(state);

        List<RetrievedChunkResponse> chunkResponses = retrievedChunks.stream()
                .map(RetrievedChunkResponse::from)
                .toList();
        return new MicroLessonResponse(state.getSessionId(), lessonMarkdown, chunkResponses, state.getCurrentStep(), "exercises");
    }

    public ExerciseQuestionSetResponse generateExercises(String sessionId) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 4, "Exercises can only be generated after micro lesson");

        if (state.getGeneratedLesson() == null || state.getGeneratedLesson().isBlank()) {
            throw BusinessException.badRequest("Micro lesson has not been generated");
        }

        List<ExerciseQuestion> exercises = exerciseGenerator.generate(state.getWeakPoints(), state.getGeneratedLesson());
        state.setExercises(exercises);
        repository.save(state);

        List<ExerciseQuestionResponse> questionResponses = exercises.stream()
                .map(ExerciseQuestionResponse::from)
                .toList();
        return new ExerciseQuestionSetResponse(state.getSessionId(), state.getCurrentStep(), "submitExercises", questionResponses);
    }

    public ExerciseSubmitResponse submitExercises(String sessionId, SubmitExerciseRequest request) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 4, "Exercise answers can only be submitted at step 4");

        if (state.getExercises().isEmpty()) {
            throw BusinessException.badRequest("Exercises have not been generated");
        }

        Map<String, String> submittedAnswers = toExerciseAnswerMap(request);
        List<ExerciseJudgeResult> results = new java.util.ArrayList<>();
        int correctCount = 0;
        for (ExerciseQuestion question : state.getExercises()) {
            String answerExpression = submittedAnswers.get(question.id());
            if (answerExpression == null) {
                throw BusinessException.badRequest("Missing answer for exercise: " + question.id());
            }
            ExerciseJudgeResult result = expressionJudgeService.judge(question, answerExpression);
            results.add(result);
            if (result.correct()) {
                correctCount++;
            }
        }

        state.setExerciseResults(results);
        state.advanceToStep(5);
        repository.save(state);

        List<ExerciseJudgeResultResponse> resultResponses = results.stream()
                .map(ExerciseJudgeResultResponse::from)
                .toList();
        double errorRate = results.isEmpty() ? 0 : (double) (results.size() - correctCount) / results.size();
        return new ExerciseSubmitResponse(
                state.getSessionId(),
                correctCount,
                results.size(),
                errorRate,
                resultResponses,
                state.getCurrentStep(),
                "review"
        );
    }

    public ReviewResponse generateReview(String sessionId) {
        LearningState state = getRequiredState(sessionId);
        ensureCurrentStep(state, 5, "Review can only be generated after exercise submission");

        if (state.getExerciseResults().isEmpty()) {
            throw BusinessException.badRequest("Exercise results have not been generated");
        }

        ReviewResult reviewResult = reviewGenerator.generate(state.getExerciseResults());
        state.finish(reviewResult);
        repository.save(state);

        List<VariantExerciseResponse> variants = reviewResult.variantExercises().stream()
                .map(VariantExerciseResponse::from)
                .toList();
        return new ReviewResponse(
                state.getSessionId(),
                reviewResult.status(),
                reviewResult.message(),
                reviewResult.errorRate(),
                reviewResult.suggestions(),
                variants,
                state.getCurrentStep(),
                "finished",
                true
        );
    }

    private LearningState getRequiredState(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> BusinessException.notFound("Learning session not found: " + sessionId));
    }

    private void ensureCurrentStep(LearningState state, int expectedStep, String message) {
        if (state.getCurrentStep() != expectedStep) {
            throw BusinessException.badRequest(message);
        }
    }

    private Map<String, String> toSubmittedAnswerMap(SubmitDiagnosisRequest request) {
        if (request == null || request.answers() == null || request.answers().isEmpty()) {
            throw BusinessException.badRequest("Diagnosis answers are required");
        }

        Map<String, String> submittedAnswers = new LinkedHashMap<>();
        for (DiagnosisAnswerRequest answer : request.answers()) {
            if (submittedAnswers.put(answer.questionId(), answer.selectedOption().trim().toUpperCase()) != null) {
                throw BusinessException.badRequest("Duplicate answer for question: " + answer.questionId());
            }
        }
        return submittedAnswers;
    }

    private void ensureOptionExists(DiagnosisQuestion question, String selectedOption) {
        boolean exists = question.options().stream()
                .anyMatch(option -> option.key().equalsIgnoreCase(selectedOption));
        if (!exists) {
            throw BusinessException.badRequest("Invalid option for question " + question.id() + ": " + selectedOption);
        }
    }

    private Map<String, String> toExerciseAnswerMap(SubmitExerciseRequest request) {
        if (request == null || request.answers() == null || request.answers().isEmpty()) {
            throw BusinessException.badRequest("Exercise answers are required");
        }

        Map<String, String> submittedAnswers = new LinkedHashMap<>();
        for (ExerciseAnswerRequest answer : request.answers()) {
            if (submittedAnswers.put(answer.questionId(), answer.answerExpression()) != null) {
                throw BusinessException.badRequest("Duplicate answer for exercise: " + answer.questionId());
            }
        }
        return submittedAnswers;
    }
}
