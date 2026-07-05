package com.agentstudy.admin;

import com.agentstudy.admin.audit.OperationLogService;
import com.agentstudy.admin.audit.dto.OperationLogResponse;
import com.agentstudy.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    public AdminOperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<OperationLogResponse>> listOperationLogs(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId
    ) {
        List<OperationLogResponse> logs = operationLogService.search(limit, operator, action, targetType, targetId).stream()
                .map(OperationLogResponse::from)
                .toList();
        return ApiResponse.success(logs);
    }
}
