package com.agentstudy.admin;

import com.agentstudy.admin.dto.AdminLoginRequest;
import com.agentstudy.admin.dto.AdminLoginResponse;
import com.agentstudy.admin.dto.AdminMeResponse;
import com.agentstudy.common.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> me(Principal principal) {
        return ApiResponse.success(new AdminMeResponse(principal.getName(), "ADMIN"));
    }
}
