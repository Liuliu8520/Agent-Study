package com.agentstudy.statistics;

import com.agentstudy.common.ApiResponse;
import com.agentstudy.statistics.dto.StatisticsDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<StatisticsDashboardResponse> getDashboard() {
        return ApiResponse.success(statisticsService.getDashboard());
    }
}
