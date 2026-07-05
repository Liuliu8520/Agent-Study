package com.agentstudy.admin.audit;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class InMemoryOperationLogRepository implements OperationLogRepository {

    private final CopyOnWriteArrayList<OperationLog> logs = new CopyOnWriteArrayList<>();

    @Override
    public OperationLog save(OperationLog log) {
        logs.add(log);
        return log;
    }

    @Override
    public List<OperationLog> search(OperationLogQuery query) {
        return logs.stream()
                .filter(log -> matches(query.operator(), log.operator()))
                .filter(log -> matches(query.action(), log.action()))
                .filter(log -> matches(query.targetType(), log.targetType()))
                .filter(log -> matches(query.targetId(), log.targetId()))
                .sorted(Comparator.comparing(OperationLog::createdAt).reversed())
                .limit(safeLimit(query.limit()))
                .toList();
    }

    private boolean matches(String expected, String actual) {
        return Optional.ofNullable(expected)
                .filter(value -> !value.isBlank())
                .map(value -> value.equalsIgnoreCase(actual))
                .orElse(true);
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), 100);
    }
}
