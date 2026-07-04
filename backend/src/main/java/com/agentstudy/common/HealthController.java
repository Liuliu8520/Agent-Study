package com.agentstudy.common;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthInfo> health() {
        return ApiResponse.success(new HealthInfo("ok", "agent-study-backend", Instant.now()));
    }

    public record HealthInfo(String status, String service, Instant timestamp) {
    }
}

