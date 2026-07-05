package com.agentstudy.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminManagementControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUpRestTemplate() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    void managesPromptTemplateVersionsAndOperationLogs() {
        String code = "custom.versioned.prompt";
        upsertPrompt(code, "v1");
        upsertPrompt(code, "v2");

        ResponseEntity<JsonNode> versionsResponse = restTemplate.exchange(
                "/api/admin/prompts/{code}/versions",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                code
        );

        assertThat(versionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode versions = versionsResponse.getBody().path("data");
        assertThat(versions.size()).isGreaterThanOrEqualTo(2);
        String v1VersionId = findVersionId(versions, "v1");
        assertThat(v1VersionId).isNotBlank();

        ResponseEntity<JsonNode> activateResponse = restTemplate.exchange(
                "/api/admin/prompts/{code}/versions/{versionId}/activate",
                HttpMethod.POST,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                code,
                v1VersionId
        );

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().path("data").path("version").asText()).isEqualTo("v1");

        ResponseEntity<JsonNode> runResponse = restTemplate.postForEntity(
                "/api/agent/mock-chat",
                Map.of(
                        "sessionId", "session-versioned-prompt",
                        "promptCode", code,
                        "variables", Map.of("topic", "chain rule")
                ),
                JsonNode.class
        );

        assertThat(runResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(runResponse.getBody().path("data").path("promptVersion").asText()).isEqualTo("v1");

        JsonNode logs = listOperationLogs("PROMPT_TEMPLATE", code);
        assertThat(logs.toString()).contains("PROMPT_TEMPLATE_UPSERT");
        assertThat(logs.toString()).contains("PROMPT_TEMPLATE_VERSION_ACTIVATE");
    }

    @Test
    void managesKnowledgeChunksVectorSearchAndOperationLogs() {
        String chunkId = "chunk-admin-vector-test";
        ResponseEntity<JsonNode> createResponse = restTemplate.exchange(
                "/api/admin/rag/chunks",
                HttpMethod.POST,
                new HttpEntity<>(knowledgeChunkBody(chunkId), adminHeaders()),
                JsonNode.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode created = createResponse.getBody().path("data");
        assertThat(created.path("id").asText()).isEqualTo(chunkId);
        assertThat(created.path("embeddingReady").asBoolean()).isTrue();
        assertThat(created.path("embeddingDimensions").asInt()).isGreaterThan(0);

        ResponseEntity<JsonNode> retrieveResponse = restTemplate.postForEntity(
                "/api/rag/retrieve",
                Map.of("keywords", List.of("admin_vector_test"), "limit", 1),
                JsonNode.class
        );

        assertThat(retrieveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retrieveResponse.getBody().path("data").get(0).path("id").asText()).isEqualTo(chunkId);

        ResponseEntity<JsonNode> embeddingResponse = restTemplate.exchange(
                "/api/admin/rag/chunks/{chunkId}/embedding",
                HttpMethod.POST,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                chunkId
        );

        assertThat(embeddingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(embeddingResponse.getBody().path("data").path("embeddingReady").asBoolean()).isTrue();

        ResponseEntity<JsonNode> deleteResponse = restTemplate.exchange(
                "/api/admin/rag/chunks/{chunkId}",
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                chunkId
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> deletedGetResponse = restTemplate.getForEntity(
                "/api/rag/chunks/{chunkId}",
                JsonNode.class,
                chunkId
        );
        assertThat(deletedGetResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        JsonNode logs = listOperationLogs("KNOWLEDGE_CHUNK", chunkId);
        assertThat(logs.toString()).contains("KNOWLEDGE_CHUNK_CREATE");
        assertThat(logs.toString()).contains("KNOWLEDGE_CHUNK_EMBEDDING_REBUILD");
        assertThat(logs.toString()).contains("KNOWLEDGE_CHUNK_DELETE");
    }

    private void upsertPrompt(String code, String version) {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/agent/prompts/{code}",
                HttpMethod.PUT,
                new HttpEntity<>(promptTemplateBody(version), adminHeaders()),
                JsonNode.class,
                code
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("data").path("version").asText()).isEqualTo(version);
    }

    private Map<String, Object> promptTemplateBody(String version) {
        return Map.of(
                "agentType", "LESSON_GENERATOR",
                "version", version,
                "name", "Versioned Lesson Agent " + version,
                "systemPrompt", "Versioned lesson system prompt " + version,
                "userPromptTemplate", "Versioned lesson " + version + " for {{topic}}"
        );
    }

    private Map<String, Object> knowledgeChunkBody(String chunkId) {
        return Map.of(
                "id", chunkId,
                "chapter", "后台知识库测试",
                "title", "后台向量检索测试切片",
                "content", "admin vector test content for chain rule derivative retrieval",
                "tags", List.of("admin_vector_test", "chain_rule")
        );
    }

    private JsonNode listOperationLogs(String targetType, String targetId) {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/admin/operation-logs?targetType={targetType}&targetId={targetId}&limit=20",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                targetType,
                targetId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().path("data");
    }

    private String findVersionId(JsonNode versions, String version) {
        for (JsonNode item : versions) {
            if (version.equals(item.path("version").asText())) {
                return item.path("versionId").asText();
            }
        }
        return "";
    }

    private HttpHeaders adminHeaders() {
        ResponseEntity<JsonNode> loginResponse = restTemplate.postForEntity(
                "/api/admin/auth/login",
                Map.of("username", "admin", "password", "agentstudy"),
                JsonNode.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginResponse.getBody().path("data").path("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
