package com.agentstudy.rag;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RagControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void listsKnowledgeChunks() {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/rag/chunks",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.size()).isGreaterThanOrEqualTo(5);
        assertThat(data.toString()).contains("chunk-chain-rule");
    }

    @Test
    void rejectsKnowledgeChunkListWithoutAdminToken() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/rag/chunks",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getsKnowledgeChunkById() {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/rag/chunks/{chunkId}",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders()),
                JsonNode.class,
                "chunk-chain-rule"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.path("id").asText()).isEqualTo("chunk-chain-rule");
        assertThat(data.path("tags").toString()).contains("chain_rule");
    }

    @Test
    void retrievesKnowledgeChunksByKeywords() {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/rag/retrieve",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("keywords", List.of("chain_rule"), "limit", 2), adminHeaders()),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = response.getBody().path("data");
        assertThat(data.size()).isGreaterThanOrEqualTo(1);
        assertThat(data.get(0).path("id").asText()).isEqualTo("chunk-chain-rule");
        assertThat(data.get(0).path("score").asDouble()).isGreaterThan(0);
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
