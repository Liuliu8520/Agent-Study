package com.agentstudy.statistics;

import com.agentstudy.agent.AgentCallLog;
import com.agentstudy.agent.AgentCallLogService;
import com.agentstudy.agent.AgentCallStatus;
import com.agentstudy.agent.AgentType;
import com.agentstudy.learn.LearningSessionRepository;
import com.agentstudy.learn.LearningSessionStatus;
import com.agentstudy.learn.LearningState;
import com.agentstudy.learn.diagnosis.WeakPoint;
import com.agentstudy.statistics.dto.AgentCallStatisticsResponse;
import com.agentstudy.statistics.dto.AgentTypeCallStatisticsResponse;
import com.agentstudy.statistics.dto.SessionStatusStatisticsResponse;
import com.agentstudy.statistics.dto.StatisticsDashboardResponse;
import com.agentstudy.statistics.dto.WeakPointStatisticResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private static final int AGENT_CALL_SAMPLE_LIMIT = 100;

    private final LearningSessionRepository learningSessionRepository;
    private final AgentCallLogService agentCallLogService;

    public StatisticsService(
            LearningSessionRepository learningSessionRepository,
            AgentCallLogService agentCallLogService
    ) {
        this.learningSessionRepository = learningSessionRepository;
        this.agentCallLogService = agentCallLogService;
    }

    public StatisticsDashboardResponse getDashboard() {
        List<LearningState> states = learningSessionRepository.findAll();
        List<AgentCallLog> agentLogs = agentCallLogService.findLatest(AGENT_CALL_SAMPLE_LIMIT);
        return new StatisticsDashboardResponse(
                buildSessionStatistics(states),
                buildWeakPointStatistics(states),
                buildAgentCallStatistics(agentLogs)
        );
    }

    private SessionStatusStatisticsResponse buildSessionStatistics(List<LearningState> states) {
        int createdSessions = countByStatus(states, LearningSessionStatus.CREATED);
        int inProgressSessions = countByStatus(states, LearningSessionStatus.IN_PROGRESS);
        int finishedSessions = countByStatus(states, LearningSessionStatus.FINISHED);
        return new SessionStatusStatisticsResponse(
                states.size(),
                createdSessions,
                inProgressSessions,
                finishedSessions
        );
    }

    private int countByStatus(List<LearningState> states, LearningSessionStatus status) {
        return (int) states.stream()
                .filter(state -> state.getStatus() == status)
                .count();
    }

    private List<WeakPointStatisticResponse> buildWeakPointStatistics(List<LearningState> states) {
        Map<String, WeakPointAccumulator> byCode = new LinkedHashMap<>();
        for (LearningState state : states) {
            for (WeakPoint weakPoint : state.getWeakPoints()) {
                byCode.computeIfAbsent(weakPoint.code(), ignored -> new WeakPointAccumulator(weakPoint))
                        .add(weakPoint);
            }
        }

        return byCode.values().stream()
                .map(WeakPointAccumulator::toResponse)
                .sorted(Comparator
                        .comparing(WeakPointStatisticResponse::count)
                        .reversed()
                        .thenComparing(WeakPointStatisticResponse::code))
                .toList();
    }

    private AgentCallStatisticsResponse buildAgentCallStatistics(List<AgentCallLog> logs) {
        long successCount = logs.stream()
                .filter(log -> log.status() == AgentCallStatus.SUCCESS)
                .count();
        long failedCount = logs.stream()
                .filter(log -> log.status() == AgentCallStatus.FAILED)
                .count();
        double averageDurationMillis = logs.stream()
                .mapToLong(AgentCallLog::durationMillis)
                .average()
                .orElse(0);
        double successRate = logs.isEmpty() ? 0 : (double) successCount / logs.size();

        return new AgentCallStatisticsResponse(
                logs.size(),
                successCount,
                failedCount,
                successRate,
                averageDurationMillis,
                buildAgentTypeStatistics(logs)
        );
    }

    private List<AgentTypeCallStatisticsResponse> buildAgentTypeStatistics(List<AgentCallLog> logs) {
        Map<AgentType, List<AgentCallLog>> byType = new EnumMap<>(AgentType.class);
        for (AgentCallLog log : logs) {
            byType.computeIfAbsent(log.agentType(), ignored -> new ArrayList<>()).add(log);
        }

        return byType.entrySet().stream()
                .map(entry -> new AgentTypeCallStatisticsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .mapToLong(AgentCallLog::durationMillis)
                                .average()
                                .orElse(0)
                ))
                .sorted(Comparator.comparing(statistic -> statistic.agentType().name()))
                .toList();
    }

    private static class WeakPointAccumulator {

        private final String code;
        private String name;
        private String latestReason;
        private long count;

        WeakPointAccumulator(WeakPoint weakPoint) {
            this.code = weakPoint.code();
            this.name = weakPoint.name();
            this.latestReason = weakPoint.reason();
        }

        void add(WeakPoint weakPoint) {
            this.name = weakPoint.name();
            this.latestReason = weakPoint.reason();
            this.count++;
        }

        WeakPointStatisticResponse toResponse() {
            return new WeakPointStatisticResponse(code, name, latestReason, count);
        }
    }
}
