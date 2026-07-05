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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgentControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void listsDefaultPromptTemplates() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/agent/prompts",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.size()).isEqualTo(5);
        assertThat(data.toString()).contains("lesson.micro");
    }

    @Test
    void runsMockLlmAndRecordsCallLog() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of(
                        "sessionId", "session-agent-test",
                        "promptCode", "lesson.micro",
                        "variables", Map.of(
                                "learningPlan", "3 天链式法则补强计划",
                                "weakPoints", "链式法则",
                                "retrievedChunks", "复合函数求导知识切片"
                        )
                ),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        String callId = data.path("callId").asText();
        assertThat(callId).isNotBlank();
        assertThat(data.path("agentType").asText()).isEqualTo("LESSON_GENERATOR");
        assertThat(data.path("modelName").asText()).isEqualTo("mock-llm-v1");
        assertThat(data.path("outputText").asText()).contains("Mock讲义");

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs/{callId}",
                JsonNode.class,
                callId
        );

        assertThat(logResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode log = logResponse.getBody().path("data");
        assertThat(log.path("callId").asText()).isEqualTo(callId);
        assertThat(log.path("status").asText()).isEqualTo("SUCCESS");
        assertThat(log.path("requestPayload").asText()).contains("3 天链式法则补强计划");
    }

    @Test
    void returnsNotFoundForUnknownPromptTemplate() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of("promptCode", "missing.prompt", "variables", Map.of()),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().path("message").asText()).contains("Prompt template not found");
    }
}
