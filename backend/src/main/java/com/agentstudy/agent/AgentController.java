package com.agentstudy.agent;

import com.agentstudy.agent.dto.AgentCallLogResponse;
import com.agentstudy.agent.dto.AgentRunResponse;
import com.agentstudy.agent.dto.PromptTemplateResponse;
import com.agentstudy.agent.dto.RunAgentRequest;
import com.agentstudy.agent.dto.UpsertPromptTemplateRequest;
import com.agentstudy.common.ApiResponse;
import jakarta.validation.Valid;
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

    public AgentController(
            PromptService promptService,
            AgentRuntimeService agentRuntimeService,
            AgentCallLogService callLogService
    ) {
        this.promptService = promptService;
        this.agentRuntimeService = agentRuntimeService;
        this.callLogService = callLogService;
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
            @Valid @RequestBody UpsertPromptTemplateRequest request
    ) {
        PromptTemplate template = promptService.saveTemplate(request.toTemplate(code));
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
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<AgentCallLogResponse> logs = callLogService.findLatest(limit).stream()
                .map(AgentCallLogResponse::from)
                .toList();
        return ApiResponse.success(logs);
    }

    @GetMapping("/call-logs/{callId}")
    public ApiResponse<AgentCallLogResponse> getCallLog(@PathVariable String callId) {
        return ApiResponse.success(AgentCallLogResponse.from(callLogService.getRequired(callId)));
    }
}
