package com.agentstudy.admin.dto;

public record AdminLoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String username
) {
}
