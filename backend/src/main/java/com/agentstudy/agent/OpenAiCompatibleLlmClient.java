package com.agentstudy.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
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
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_CONFIGURATION,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "LLM base-url or api-key is not configured"
            );
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    buildHttpRequest(request),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() >= 400) {
                return fallbackOrThrow(
                        request,
                        classifyHttpError(response.statusCode(), response.body()),
                        statusForHttpError(response.statusCode()),
                        buildHttpErrorMessage(response.statusCode(), response.body())
                );
            }
            return parseResponse(response.body());
        } catch (LlmException exception) {
            return fallbackOrThrow(
                    request,
                    exception.getErrorType(),
                    exception.getStatus(),
                    exception.getMessage()
            );
        } catch (HttpTimeoutException exception) {
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_TIMEOUT,
                    HttpStatus.GATEWAY_TIMEOUT,
                    "LLM request timed out"
            );
        } catch (IOException exception) {
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_NETWORK,
                    HttpStatus.BAD_GATEWAY,
                    "LLM network request failed: " + exception.getMessage()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_TIMEOUT,
                    HttpStatus.GATEWAY_TIMEOUT,
                    "LLM request was interrupted"
            );
        } catch (IllegalArgumentException exception) {
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_ENDPOINT,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "LLM base-url is invalid: " + exception.getMessage()
            );
        } catch (RuntimeException exception) {
            return fallbackOrThrow(
                    request,
                    LlmErrorType.LLM_RESPONSE_FORMAT,
                    HttpStatus.BAD_GATEWAY,
                    "LLM response handling failed: " + exception.getMessage()
            );
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

    private LlmResponse parseResponse(String body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new LlmException(
                    LlmErrorType.LLM_RESPONSE_FORMAT,
                    HttpStatus.BAD_GATEWAY,
                    "LLM response is not valid JSON"
            );
        }
        String content = root.path("choices").path(0).path("message").path("content").asText();
        if (!StringUtils.hasText(content)) {
            throw new LlmException(
                    LlmErrorType.LLM_RESPONSE_FORMAT,
                    HttpStatus.BAD_GATEWAY,
                    "LLM response content is empty"
            );
        }
        JsonNode usage = root.path("usage");
        return new LlmResponse(
                root.path("model").asText(properties.getModel()),
                content,
                usage.path("prompt_tokens").asInt(0),
                usage.path("completion_tokens").asInt(0)
        );
    }

    private LlmResponse fallbackOrThrow(LlmRequest request, LlmErrorType errorType, HttpStatus status, String reason) {
        if (properties.isFallbackToMock()) {
            return MockLlmClient.completeMock(request);
        }
        throw new LlmException(errorType, status, reason);
    }

    private LlmErrorType classifyHttpError(int statusCode, String body) {
        String lowerBody = body == null ? "" : body.toLowerCase();
        if (statusCode == 401 || statusCode == 403) {
            return LlmErrorType.LLM_AUTHENTICATION;
        }
        if (statusCode == 404) {
            return LlmErrorType.LLM_ENDPOINT;
        }
        if (statusCode == 429) {
            return LlmErrorType.LLM_RATE_LIMIT;
        }
        if (lowerBody.contains("model")) {
            return LlmErrorType.LLM_MODEL;
        }
        return LlmErrorType.LLM_UPSTREAM;
    }

    private HttpStatus statusForHttpError(int statusCode) {
        if (statusCode == 429) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (statusCode >= 500) {
            return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.BAD_GATEWAY;
    }

    private String buildHttpErrorMessage(int statusCode, String body) {
        String upstreamMessage = extractUpstreamMessage(body);
        if (StringUtils.hasText(upstreamMessage)) {
            return "LLM request failed with HTTP " + statusCode + ": " + upstreamMessage;
        }
        return "LLM request failed with HTTP " + statusCode;
    }

    private String extractUpstreamMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = root.path("error").path("message").asText();
            if (!StringUtils.hasText(message)) {
                message = root.path("message").asText();
            }
            if (!StringUtils.hasText(message)) {
                message = root.path("msg").asText();
            }
            return trimMessage(message);
        } catch (JsonProcessingException exception) {
            return trimMessage(body);
        }
    }

    private String trimMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() > 500 ? normalized.substring(0, 500) + "..." : normalized;
    }
}
