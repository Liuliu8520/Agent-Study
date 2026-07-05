package com.agentstudy.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
        assertThat(data.size()).isGreaterThanOrEqualTo(5);
        assertThat(data.toString()).contains("lesson.micro");
    }

    @Test
    void upsertsPromptTemplateAndUsesItInMockRun() {
        ResponseEntity<JsonNode> upsertResponse = restTemplate.exchange(
                "/api/agent/prompts/{code}",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "agentType", "LESSON_GENERATOR",
                        "version", "v2",
                        "name", "Custom Lesson Agent",
                        "systemPrompt", "Custom lesson system prompt",
                        "userPromptTemplate", "Custom lesson for {{topic}}"
                )),
                JsonNode.class,
                "custom.lesson"
        );

        assertThat(upsertResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode template = upsertResponse.getBody().path("data");
        assertThat(template.path("code").asText()).isEqualTo("custom.lesson");
        assertThat(template.path("version").asText()).isEqualTo("v2");

        ResponseEntity<JsonNode> runResponse = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of(
                        "sessionId", "session-custom-prompt",
                        "promptCode", "custom.lesson",
                        "variables", Map.of("topic", "chain rule")
                ),
                JsonNode.class
        );

        assertThat(runResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode run = runResponse.getBody().path("data");
        assertThat(run.path("promptVersion").asText()).isEqualTo("v2");
        String callId = run.path("callId").asText();

        ResponseEntity<JsonNode> logResponse = restTemplate.getForEntity(
                "/api/agent/call-logs/{callId}",
                JsonNode.class,
                callId
        );
        JsonNode log = logResponse.getBody().path("data");
        assertThat(log.path("promptVersion").asText()).isEqualTo("v2");
        assertThat(log.path("requestPayload").asText()).contains("Custom lesson system prompt");
        assertThat(log.path("requestPayload").asText()).contains("Custom lesson for chain rule");
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
