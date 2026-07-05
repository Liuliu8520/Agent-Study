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
                "agent-study.llm.fallback-to-mock=true"
        }
)
class OpenAiCompatibleLlmClientFallbackTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fallsBackToMockWhenRealLlmConfigIsMissing() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of(
                        "sessionId", "session-llm-fallback-test",
                        "promptCode", "lesson.micro",
                        "variables", Map.of(
                                "learningPlan", "fallback plan",
                                "weakPoints", "chain rule",
                                "retrievedChunks", "chunk"
                        )
                ),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("modelName").asText()).isEqualTo("mock-llm-v1");
        assertThat(data.path("promptCode").asText()).isEqualTo("lesson.micro");
        assertThat(data.path("sessionId").asText()).isEqualTo("session-llm-fallback-test");
    }
}
