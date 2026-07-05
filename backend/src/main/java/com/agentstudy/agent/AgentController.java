package com.agentstudy.agent;

import com.agentstudy.admin.audit.OperationLogService;
import com.agentstudy.agent.dto.AgentCallLogResponse;
import com.agentstudy.agent.dto.AgentRunResponse;
import com.agentstudy.agent.dto.PromptTemplateResponse;
import com.agentstudy.agent.dto.RunAgentRequest;
import com.agentstudy.agent.dto.UpsertPromptTemplateRequest;
import com.agentstudy.common.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final PromptService promptService;
    private final AgentRuntimeService agentRuntimeService;
    private final AgentCallLogService callLogService;
    private final OperationLogService operationLogService;

    public AgentController(
            PromptService promptService,
            AgentRuntimeService agentRuntimeService,
            AgentCallLogService callLogService,
            OperationLogService operationLogService
    ) {
        this.promptService = promptService;
        this.agentRuntimeService = agentRuntimeService;
        this.callLogService = callLogService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/prompts")
    public ApiResponse<List<PromptTemplateResponse>> listPrompts() {
        List<PromptTemplateResponse> templates = promptService.listTemplates().stream()
                .map(PromptTemplateResponse::from)
                .toList();
        return ApiResponse.success(templates);
    }

    @PutMapping("/prompts/{code}")
    public ApiResponse<PromptTemplateResponse> upsertPrompt(
            @PathVariable String code,
            @Valid @RequestBody UpsertPromptTemplateRequest request,
            Principal principal
    ) {
        String operator = principal == null ? "system" : principal.getName();
        PromptTemplate template = promptService.saveTemplate(request.toTemplate(code), operator);
        operationLogService.record(
                operator,
                "PROMPT_TEMPLATE_UPSERT",
                "PROMPT_TEMPLATE",
                code,
                "Saved prompt template version " + template.version()
        );
        return ApiResponse.success(PromptTemplateResponse.from(template));
    }

    @PostMapping("/mock-chat")
    public ApiResponse<AgentRunResponse> runMockAgent(@Valid @RequestBody RunAgentRequest request) {
        AgentRunResult result = agentRuntimeService.run(new AgentRunCommand(
                request.sessionId(),
                request.promptCode(),
                request.variables()
        ));
        return ApiResponse.success(AgentRunResponse.from(result));
    }

    @GetMapping("/call-logs")
    public ApiResponse<List<AgentCallLogResponse>> listCallLogs(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) AgentType agentType,
            @RequestParam(required = false) AgentCallStatus status,
            @RequestParam(required = false) String promptCode
    ) {
        List<AgentCallLogResponse> logs = callLogService.search(limit, sessionId, agentType, status, promptCode).stream()
                .map(AgentCallLogResponse::from)
                .toList();
        return ApiResponse.success(logs);
    }

    @GetMapping("/call-logs/{callId}")
    public ApiResponse<AgentCallLogResponse> getCallLog(@PathVariable String callId) {
        return ApiResponse.success(AgentCallLogResponse.from(callLogService.getRequired(callId)));
    }
}
