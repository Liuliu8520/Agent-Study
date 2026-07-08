package com.agentstudy.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "agent-study.llm.provider=openai-compatible",
                "agent-study.llm.base-url=",
                "agent-study.llm.api-key=",
                "agent-study.llm.fallback-to-mock=false"
        }
)
class OpenAiCompatibleLlmClientFailureTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void classifiesMissingRealLlmConfigAndRecordsFailedCallLog() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of(
                        "sessionId", "session-llm-config-failure-test",
                        "promptCode", "lesson.micro",
                        "variables", Map.of(
                                "learningPlan", "real llm plan",
                                "weakPoints", "chain rule",
                                "retrievedChunks", "chunk"
                        )
                ),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().path("errorType").asText()).isEqualTo("LLM_CONFIGURATION");
        assertThat(response.getBody().path("message").asText()).contains("base-url or api-key");

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs?sessionId={sessionId}&status=FAILED&limit=5",
                JsonNode.class,
                "session-llm-config-failure-test"
        );

        assertThat(logResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode logs = logResponse.getBody().path("data");
        assertThat(logs.size()).isGreaterThanOrEqualTo(1);
        JsonNode latestLog = logs.get(0);
        assertThat(latestLog.path("status").asText()).isEqualTo("FAILED");
        assertThat(latestLog.path("errorMessage").asText()).contains("base-url or api-key");
        assertThat(latestLog.path("totalTokens").asInt()).isEqualTo(0);
    }
}
