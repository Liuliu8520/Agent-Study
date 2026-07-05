package com.agentstudy.admin.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AdminSecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(AdminSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getTokenTtlMinutes() * 60);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", username);
        payload.put("role", "ADMIN");
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public String validateAndGetSubject(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT token is required");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("JWT token format is invalid");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("JWT token signature is invalid");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        long expiresAt = toLong(payload.get("exp"));
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("JWT token is expired");
        }

        Object subject = payload.get("sub");
        if (!(subject instanceof String username) || !StringUtils.hasText(username)) {
            throw new IllegalArgumentException("JWT token subject is invalid");
        }
        return username;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return base64UrlEncode(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to encode JWT JSON", exception);
        }
    }

    private Map<String, Object> decodeJson(String encodedValue) {
        try {
            return objectMapper.readValue(Base64.getUrlDecoder().decode(encodedValue), MAP_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT token payload is invalid", exception);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT token", exception);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("JWT token exp is invalid");
    }
}
