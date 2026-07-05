package com.agentstudy.statistics;

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
class StatisticsControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getsDashboardStatistics() {
        String sessionId = createSession("StatsUser");
        generateDiagnosisQuestions(sessionId);
        submitDiagnosis(sessionId, List.of(
                answer("limit-basic", "A"),
                answer("derivative-power", "C"),
                answer("derivative-chain", "A"),
                answer("integral-basic", "D"),
                answer("integral-definite", "A")
        ));

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/statistics/dashboard",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("sessions").path("totalSessions").asInt()).isGreaterThan(0);
        assertThat(data.path("sessions").path("inProgressSessions").asInt()).isGreaterThan(0);
        assertThat(data.path("weakPoints").toString()).contains("chain_rule");
        assertThat(data.path("agentCalls").path("sampleSize").asInt()).isGreaterThan(0);
        assertThat(data.path("agentCalls").path("successCount").asInt()).isGreaterThan(0);
        assertThat(data.path("agentCalls").path("byAgentType").toString()).contains("DIAGNOSTICIAN");
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

    private void submitDiagnosis(String sessionId, List<Map<String, String>> answers) {
        restTemplate.postForEntity(
                "/api/learn/sessions/{sessionId}/diagnosis/submit",
                Map.of("answers", answers),
                JsonNode.class,
                sessionId
        );
    }

    private Map<String, String> answer(String questionId, String selectedOption) {
        return Map.of("questionId", questionId, "selectedOption", selectedOption);
    }
}
