package com.agentstudy.admin;

import com.agentstudy.admin.audit.OperationLogService;
import com.agentstudy.agent.PromptService;
import com.agentstudy.agent.PromptTemplate;
import com.agentstudy.agent.dto.PromptTemplateResponse;
import com.agentstudy.agent.dto.PromptTemplateVersionResponse;
import com.agentstudy.common.ApiResponse;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/prompts")
public class AdminPromptController {

    private final PromptService promptService;
    private final OperationLogService operationLogService;

    public AdminPromptController(PromptService promptService, OperationLogService operationLogService) {
        this.promptService = promptService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/{code}/versions")
    public ApiResponse<List<PromptTemplateVersionResponse>> listVersions(@PathVariable String code) {
        List<PromptTemplateVersionResponse> versions = promptService.listVersions(code).stream()
                .map(PromptTemplateVersionResponse::from)
                .toList();
        return ApiResponse.success(versions);
    }

    @PostMapping("/{code}/versions/{versionId}/activate")
    public ApiResponse<PromptTemplateResponse> activateVersion(
            @PathVariable String code,
            @PathVariable String versionId,
            Principal principal
    ) {
        PromptTemplate template = promptService.activateVersion(code, versionId);
        String operator = principal == null ? "system" : principal.getName();
        operationLogService.record(
                operator,
                "PROMPT_TEMPLATE_VERSION_ACTIVATE",
                "PROMPT_TEMPLATE",
                code,
                "Activated prompt template version id " + versionId
        );
        return ApiResponse.success(PromptTemplateResponse.from(template));
    }
}
