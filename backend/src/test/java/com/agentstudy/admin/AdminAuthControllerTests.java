package com.agentstudy.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
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
class AdminAuthControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUpRestTemplate() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    void rejectsAdminRequestWithoutToken() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/admin/me",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().path("code").asInt()).isEqualTo(-1);
    }

    @Test
    void logsInAndGetsCurrentAdmin() {
        ResponseEntity<JsonNode> loginResponse = login("admin", "agentstudy");

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode loginData = loginResponse.getBody().path("data");
        assertThat(loginData.path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(loginData.path("accessToken").asText()).isNotBlank();
        assertThat(loginData.path("username").asText()).isEqualTo("admin");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginData.path("accessToken").asText());
        ResponseEntity<JsonNode> meResponse = restTemplate.exchange(
                "/api/admin/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );

        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode me = meResponse.getBody().path("data");
        assertThat(me.path("username").asText()).isEqualTo("admin");
        assertThat(me.path("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void rejectsInvalidAdminPassword() {
        ResponseEntity<JsonNode> response = login("admin", "wrong-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().path("message").asText()).contains("Invalid admin username or password");
    }

    @Test
    void keepsLearningApiPublic() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/learn/sessions",
                Map.of("studentName", "PublicUser"),
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("data").path("studentName").asText()).isEqualTo("PublicUser");
    }

    private ResponseEntity<JsonNode> login(String username, String password) {
        return restTemplate.postForEntity(
                "/api/admin/auth/login",
                Map.of("username", username, "password", password),
                JsonNode.class
        );
    }
}
