package com.agentstudy.learn;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LearningControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createsLearningSession() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/learn/sessions",
                Map.of("studentName", "Alice"),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("code").asInt()).isEqualTo(0);
        assertThat(body.path("data").path("sessionId").asText()).isNotBlank();
        assertThat(body.path("data").path("studentName").asText()).isEqualTo("Alice");
        assertThat(body.path("data").path("currentStep").asInt()).isEqualTo(1);
        assertThat(body.path("data").path("status").asText()).isEqualTo("CREATED");
        assertThat(body.path("data").path("nextAction").asText()).isEqualTo("diagnosis");
    }

    @Test
    void getsLearningSessionById() {
        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                "/api/learn/sessions",
                Map.of("studentName", "Bob"),
                JsonNode.class
        );
        String sessionId = createResponse.getBody().path("data").path("sessionId").asText();

        ResponseEntity<JsonNode> getResponse = restTemplate.getForEntity(
                "/api/learn/sessions/{sessionId}",
                JsonNode.class,
                sessionId
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = getResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("code").asInt()).isEqualTo(0);
        assertThat(body.path("data").path("sessionId").asText()).isEqualTo(sessionId);
        assertThat(body.path("data").path("studentName").asText()).isEqualTo("Bob");
    }

    @Test
    void defaultsStudentNameWhenStudentNameIsMissing() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/learn/sessions",
                Map.of(),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("data").path("studentName").asText()).isEqualTo("anonymous");
    }

    @Test
    void returnsNotFoundForMissingSession() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/learn/sessions/{sessionId}",
                JsonNode.class,
                "missing-session"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("code").asInt()).isEqualTo(-1);
        assertThat(body.path("message").asText()).contains("Learning session not found");
    }

    @Test
    void generatesDiagnosisQuestions() {
        String sessionId = createSession("Carol");

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/diagnosis/questions",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("sessionId").asText()).isEqualTo(sessionId);
        assertThat(data.path("currentStep").asInt()).isEqualTo(1);
        assertThat(data.path("nextAction").asText()).isEqualTo("submitDiagnosis");
        assertThat(data.path("questions").size()).isEqualTo(5);
        assertThat(data.path("questions").get(0).has("correctOptionKey")).isFalse();
    }

    @Test
    void submitsDiagnosisAndAdvancesToPlanStepWhenAllCorrect() {
        String sessionId = createSession("Dana");
        generateDiagnosisQuestions(sessionId);

        ResponseEntity<JsonNode> response = submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "B"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("correctCount").asInt()).isEqualTo(5);
        assertThat(data.path("totalCount").asInt()).isEqualTo(5);
        assertThat(data.path("weakPoints").size()).isEqualTo(0);
        assertThat(data.path("currentStep").asInt()).isEqualTo(2);
        assertThat(data.path("nextAction").asText()).isEqualTo("plan");

        ResponseEntity<JsonNode> getResponse = restTemplate.getForEntity(
                "/api/learn/sessions/{sessionId}",
                JsonNode.class,
                sessionId
        );
        assertThat(getResponse.getBody().path("data").path("currentStep").asInt()).isEqualTo(2);
        assertThat(getResponse.getBody().path("data").path("nextAction").asText()).isEqualTo("plan");

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?limit=10",
                JsonNode.class
        );
        assertThat(logResponse.getBody().path("data").toString()).contains(sessionId);
        assertThat(logResponse.getBody().path("data").toString()).contains("diagnosis.default");
    }

    @Test
    void submitsDiagnosisAndReturnsWeakPointsWhenAnswersAreWrong() {
        String sessionId = createSession("Erin");
        generateDiagnosisQuestions(sessionId);

        ResponseEntity<JsonNode> response = submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "A"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode weakPoints = response.getBody().path("data").path("weakPoints");
        assertThat(weakPoints.size()).isEqualTo(1);
        assertThat(weakPoints.get(0).path("code").asText()).isEqualTo("chain_rule");
        assertThat(weakPoints.get(0).path("name").asText()).isEqualTo("链式法则");
    }

    @Test
    void rejectsDiagnosisSubmitBeforeQuestionGeneration() {
        String sessionId = createSession("Frank");

        ResponseEntity<JsonNode> response = submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).contains("Diagnosis questions have not been generated");
    }

    @Test
    void generatesLearningPlanFromWeakPoints() {
        String sessionId = createSession("Grace");
        generateDiagnosisQuestions(sessionId);
        submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "A"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));

        ResponseEntity<JsonNode> response = generateLearningPlan(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("currentStep").asInt()).isEqualTo(3);
        assertThat(data.path("nextAction").asText()).isEqualTo("lesson");
        assertThat(data.path("plan").path("title").asText()).isEqualTo("3 天高数薄弱点补强计划");
        assertThat(data.path("plan").path("days").size()).isEqualTo(3);
        assertThat(data.path("plan").path("days").get(0).path("concepts").toString()).contains("链式法则");

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?limit=10",
                JsonNode.class
        );
        assertThat(logResponse.getBody().path("data").toString()).contains(sessionId);
        assertThat(logResponse.getBody().path("data").toString()).contains("planner.three-day");
    }

    @Test
    void generatesConsolidationPlanWhenDiagnosisIsAllCorrect() {
        String sessionId = createSession("Helen");
        generateDiagnosisQuestions(sessionId);
        submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "B"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));

        ResponseEntity<JsonNode> response = generateLearningPlan(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("plan").path("title").asText()).isEqualTo("3 天高数巩固提升计划");
        assertThat(data.path("plan").path("days").size()).isEqualTo(3);
        assertThat(data.path("currentStep").asInt()).isEqualTo(3);
    }

    @Test
    void rejectsLearningPlanBeforeDiagnosisSubmit() {
        String sessionId = createSession("Ivy");

        ResponseEntity<JsonNode> response = generateLearningPlan(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).contains("Learning plan can only be generated after diagnosis");
    }

    @Test
    void generatesMicroLessonAfterLearningPlan() {
        String sessionId = createSession("Jack");
        generateDiagnosisQuestions(sessionId);
        submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "A"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));
        generateLearningPlan(sessionId);

        ResponseEntity<JsonNode> response = generateMicroLesson(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("currentStep").asInt()).isEqualTo(4);
        assertThat(data.path("nextAction").asText()).isEqualTo("exercises");
        assertThat(data.path("lessonMarkdown").asText()).contains("个性化高数微讲义");
        assertThat(data.path("lessonMarkdown").asText()).contains("链式法则");
        assertThat(data.path("lessonMarkdown").asText()).contains("Agent 辅助生成摘要");
        assertThat(data.path("lessonMarkdown").asText()).contains("Mock讲义");
        assertThat(data.path("retrievedChunks").size()).isGreaterThan(0);

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?limit=10",
                JsonNode.class
        );
        assertThat(logResponse.getBody().path("data").toString()).contains(sessionId);
        assertThat(logResponse.getBody().path("data").toString()).contains("lesson.micro");
    }

    @Test
    void rejectsMicroLessonBeforeLearningPlan() {
        String sessionId = createSession("Kate");

        ResponseEntity<JsonNode> response = generateMicroLesson(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).contains("Micro lesson can only be generated after learning plan");
    }

    @Test
    void generatesExercisesAfterMicroLesson() {
        String sessionId = createSession("Leo");
        completeToMicroLesson(sessionId);

        ResponseEntity<JsonNode> response = generateExercises(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("currentStep").asInt()).isEqualTo(4);
        assertThat(data.path("nextAction").asText()).isEqualTo("submitExercises");
        assertThat(data.path("questions").size()).isEqualTo(3);
        assertThat(data.path("questions").get(0).has("standardAnswer")).isFalse();

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?limit=10",
                JsonNode.class
        );
        assertThat(logResponse.getBody().path("data").toString()).contains(sessionId);
        assertThat(logResponse.getBody().path("data").toString()).contains("exercise.generate");
    }

    @Test
    void submitsExercisesAndAdvancesToReviewWhenAllCorrect() {
        String sessionId = createSession("Mia");
        completeToMicroLesson(sessionId);
        generateExercises(sessionId);

        ResponseEntity<JsonNode> response = submitExercises(sessionId, List.of(
                exerciseAnswer("exercise-power-derivative", "3*x^2"),
                exerciseAnswer("exercise-chain-derivative", "2*x*cos(x^2)"),
                exerciseAnswer("exercise-basic-integral", "x*x")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("correctCount").asInt()).isEqualTo(3);
        assertThat(data.path("totalCount").asInt()).isEqualTo(3);
        assertThat(data.path("errorRate").asDouble()).isEqualTo(0.0);
        assertThat(data.path("currentStep").asInt()).isEqualTo(5);
        assertThat(data.path("nextAction").asText()).isEqualTo("review");
        assertThat(data.path("results").get(2).path("correct").asBoolean()).isTrue();
    }

    @Test
    void submitsExercisesAndMarksWrongExpressions() {
        String sessionId = createSession("Nina");
        completeToMicroLesson(sessionId);
        generateExercises(sessionId);

        ResponseEntity<JsonNode> response = submitExercises(sessionId, List.of(
                exerciseAnswer("exercise-power-derivative", "3*x^2"),
                exerciseAnswer("exercise-chain-derivative", "cos(x^2)"),
                exerciseAnswer("exercise-basic-integral", "x^2")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("correctCount").asInt()).isEqualTo(2);
        assertThat(data.path("errorRate").asDouble()).isGreaterThan(0);
        assertThat(data.path("results").get(1).path("correct").asBoolean()).isFalse();
        assertThat(data.path("results").get(1).path("detail").asText()).contains("Mismatch");
    }

    @Test
    void rejectsExercisesBeforeMicroLesson() {
        String sessionId = createSession("Oscar");

        ResponseEntity<JsonNode> response = generateExercises(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).contains("Exercises can only be generated after micro lesson");
    }

    @Test
    void generatesCompletionReviewWhenAllExercisesAreCorrect() {
        String sessionId = createSession("Pam");
        completeToExercises(sessionId);
        submitExercises(sessionId, List.of(
                exerciseAnswer("exercise-power-derivative", "3*x^2"),
                exerciseAnswer("exercise-chain-derivative", "2*x*cos(x^2)"),
                exerciseAnswer("exercise-basic-integral", "x^2")
        ));

        ResponseEntity<JsonNode> response = generateReview(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(data.path("finished").asBoolean()).isTrue();
        assertThat(data.path("variantExercises").size()).isEqualTo(0);

        ResponseEntity<JsonNode> getResponse = restTemplate.getForEntity(
                "/api/learn/sessions/{sessionId}",
                JsonNode.class,
                sessionId
        );
        assertThat(getResponse.getBody().path("data").path("status").asText()).isEqualTo("FINISHED");
        assertThat(getResponse.getBody().path("data").path("nextAction").asText()).isEqualTo("finished");

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?limit=10",
                JsonNode.class
        );
        assertThat(logResponse.getBody().path("data").toString()).contains(sessionId);
        assertThat(logResponse.getBody().path("data").toString()).contains("review.feedback");
    }

    @Test
    void generatesReinforcementReviewWhenErrorRateIsHigh() {
        String sessionId = createSession("Quinn");
        completeToExercises(sessionId);
        submitExercises(sessionId, List.of(
                exerciseAnswer("exercise-power-derivative", "x^2"),
                exerciseAnswer("exercise-chain-derivative", "cos(x^2)"),
                exerciseAnswer("exercise-basic-integral", "x^2")
        ));

        ResponseEntity<JsonNode> response = generateReview(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("status").asText()).isEqualTo("NEEDS_REINFORCEMENT");
        assertThat(data.path("errorRate").asDouble()).isGreaterThanOrEqualTo(0.5);
        assertThat(data.path("variantExercises").size()).isEqualTo(2);
        assertThat(data.path("variantExercises").get(0).has("standardAnswer")).isFalse();
    }

    @Test
    void generatesSuggestedReviewWhenErrorRateIsLow() {
        String sessionId = createSession("Rita");
        completeToExercises(sessionId);
        submitExercises(sessionId, List.of(
                exerciseAnswer("exercise-power-derivative", "3*x^2"),
                exerciseAnswer("exercise-chain-derivative", "cos(x^2)"),
                exerciseAnswer("exercise-basic-integral", "x^2")
        ));

        ResponseEntity<JsonNode> response = generateReview(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("status").asText()).isEqualTo("REVIEW_SUGGESTED");
        assertThat(data.path("variantExercises").size()).isEqualTo(0);
        assertThat(data.path("suggestions").size()).isGreaterThan(0);
    }

    @Test
    void rejectsReviewBeforeExerciseSubmission() {
        String sessionId = createSession("Sam");

        ResponseEntity<JsonNode> response = generateReview(sessionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("message").asText()).contains("Review can only be generated after exercise submission");
    }

    private String createSession(String studentName) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/learn/sessions",
                Map.of("studentName", studentName),
                JsonNode.class
        );
        return response.getBody().path("data").path("sessionId").asText();
    }

    private void generateDiagnosisQuestions(String sessionId) {
        restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/diagnosis/questions",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> submitDiagnosis(String sessionId, List<Map<String, String>> answers) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/diagnosis/submit",
                Map.of("answers", answers),
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> generateLearningPlan(String sessionId) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/plan",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> generateMicroLesson(String sessionId) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/lesson",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> generateExercises(String sessionId) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/exercises",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> submitExercises(String sessionId, List<Map<String, String>> answers) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/exercises/submit",
                Map.of("answers", answers),
                JsonNode.class,
                sessionId
        );
    }

    private ResponseEntity<JsonNode> generateReview(String sessionId) {
        return restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/review",
                HttpEntity.EMPTY,
                JsonNode.class,
                sessionId
        );
    }

    private void completeToMicroLesson(String sessionId) {
        generateDiagnosisQuestions(sessionId);
        submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "B"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));
        generateLearningPlan(sessionId);
        generateMicroLesson(sessionId);
    }

    private void completeToExercises(String sessionId) {
        completeToMicroLesson(sessionId);
        generateExercises(sessionId);
    }

    private Map<String, String> exerciseAnswer(String questionId, String answerExpression) {
        return Map.of("questionId", questionId, "answerExpression", answerExpression);
    }

    private Map<String, String> answer(String questionId, String selectedOption) {
        return Map.of("questionId", questionId, "selectedOption", selectedOption);
    }
}
