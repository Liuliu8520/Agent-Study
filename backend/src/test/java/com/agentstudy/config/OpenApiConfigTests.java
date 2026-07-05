package com.agentstudy.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiConfigTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void exposesGroupedOpenApiDocument() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/v3/api-docs/agent-study",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body.path("info").path("title").asText()).isEqualTo("Agent Study API");
        assertThat(body.path("paths").has("/api/learn/sessions")).isTrue();
        assertThat(body.path("paths").has("/api/agent/prompts")).isTrue();
    }
}
