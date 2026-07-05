package com.agentstudy.admin;

import com.agentstudy.admin.dto.AdminLoginRequest;
import com.agentstudy.admin.dto.AdminLoginResponse;
import com.agentstudy.admin.security.AdminSecurityProperties;
import com.agentstudy.admin.security.JwtService;
import com.agentstudy.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminSecurityProperties properties;
    private final JwtService jwtService;

    public AdminAuthService(AdminSecurityProperties properties, JwtService jwtService) {
        this.properties = properties;
        this.jwtService = jwtService;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!matches(request.username(), properties.getUsername()) || !matches(request.password(), properties.getPassword())) {
            throw BusinessException.unauthorized("Invalid admin username or password");
        }

        String token = jwtService.generateToken(properties.getUsername());
        return new AdminLoginResponse(
                "Bearer",
                token,
                properties.getTokenTtlMinutes() * 60,
                properties.getUsername()
        );
    }

    private boolean matches(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
