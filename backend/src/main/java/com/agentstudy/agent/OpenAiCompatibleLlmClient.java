package com.agentstudy.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "agent-study.llm", name = "provider", havingValue = "openai-compatible")
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final LlmClientProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleLlmClient(
            LlmClientProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        if (!hasRequiredConfig()) {
            return fallbackOrThrow(request, "LLM base-url or api-key is not configured");
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    buildHttpRequest(request),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() >= 400) {
                return fallbackOrThrow(request, "LLM request failed with HTTP " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (IOException exception) {
            return fallbackOrThrow(request, "LLM request failed: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return fallbackOrThrow(request, "LLM request was interrupted");
        } catch (RuntimeException exception) {
            return fallbackOrThrow(request, "LLM response handling failed: " + exception.getMessage());
        }
    }

    private boolean hasRequiredConfig() {
        return StringUtils.hasText(properties.getBaseUrl()) && StringUtils.hasText(properties.getApiKey());
    }

    private HttpRequest buildHttpRequest(LlmRequest request) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl()))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(buildPayload(request))))
                .build();
    }

    private Map<String, Object> buildPayload(LlmRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", buildMessages(request));
        payload.put("temperature", properties.getTemperature());
        if (properties.getMaxTokens() > 0) {
            payload.put("max_tokens", properties.getMaxTokens());
        }
        return payload;
    }

    private List<Map<String, String>> buildMessages(LlmRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        messages.add(Map.of("role", "user", "content", request.userPrompt()));
        return messages;
    }

    private LlmResponse parseResponse(String body) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(body);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("LLM response content is empty");
        }
        JsonNode usage = root.path("usage");
        return new LlmResponse(
                root.path("model").asText(properties.getModel()),
                content,
                usage.path("prompt_tokens").asInt(0),
                usage.path("completion_tokens").asInt(0)
        );
    }

    private LlmResponse fallbackOrThrow(LlmRequest request, String reason) {
        if (properties.isFallbackToMock()) {
            return MockLlmClient.completeMock(request);
        }
        throw new IllegalStateException(reason);
    }
}
