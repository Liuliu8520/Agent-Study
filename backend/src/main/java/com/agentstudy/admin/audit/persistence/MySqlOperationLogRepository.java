package com.agentstudy.admin.audit.persistence;

import com.agentstudy.admin.audit.OperationLog;
import com.agentstudy.admin.audit.OperationLogQuery;
import com.agentstudy.admin.audit.OperationLogRepository;
import com.agentstudy.learn.persistence.LearningPersistenceProperties;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Profile("dev")
public class MySqlOperationLogRepository implements OperationLogRepository {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS operation_log (
                log_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'operation log id',
                operator VARCHAR(100) NOT NULL COMMENT 'operator username',
                action VARCHAR(100) NOT NULL COMMENT 'operation action',
                target_type VARCHAR(100) NOT NULL COMMENT 'target type',
                target_id VARCHAR(128) NOT NULL COMMENT 'target id',
                detail TEXT NULL COMMENT 'operation detail',
                created_at DATETIME(6) NOT NULL COMMENT 'created time',
                KEY idx_operation_log_operator_created (operator, created_at),
                KEY idx_operation_log_target_created (target_type, target_id, created_at),
                KEY idx_operation_log_action_created (action, created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String INSERT_SQL = """
            INSERT INTO operation_log (
                log_id, operator, action, target_type, target_id, detail, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final LearningPersistenceProperties properties;

    public MySqlOperationLogRepository(LearningPersistenceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize operation_log table", exception);
        }
    }

    @Override
    public OperationLog save(OperationLog log) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, log.logId());
            statement.setString(2, log.operator());
            statement.setString(3, log.action());
            statement.setString(4, log.targetType());
            statement.setString(5, log.targetId());
            statement.setString(6, log.detail());
            statement.setTimestamp(7, Timestamp.from(log.createdAt()));
            statement.executeUpdate();
            return log;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save operation log: " + log.logId(), exception);
        }
    }

    @Override
    public List<OperationLog> search(OperationLogQuery query) {
        StringBuilder sql = new StringBuilder("""
                SELECT log_id, operator, action, target_type, target_id, detail, created_at
                FROM operation_log
                WHERE 1 = 1
                """);
        List<String> parameters = new ArrayList<>();
        appendFilter(sql, parameters, "operator", query.operator());
        appendFilter(sql, parameters, "action", query.action());
        appendFilter(sql, parameters, "target_type", query.targetType());
        appendFilter(sql, parameters, "target_id", query.targetId());
        sql.append(" ORDER BY created_at DESC LIMIT ?");

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setString(index + 1, parameters.get(index));
            }
            statement.setInt(parameters.size() + 1, Math.min(Math.max(query.limit(), 1), 100));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OperationLog> logs = new ArrayList<>();
                while (resultSet.next()) {
                    logs.add(mapRow(resultSet));
                }
                return logs;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to search operation logs", exception);
        }
    }

    private void appendFilter(StringBuilder sql, List<String> parameters, String column, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        sql.append(" AND ").append(column).append(" = ?");
        parameters.add(value);
    }

    private OperationLog mapRow(ResultSet resultSet) throws SQLException {
        return new OperationLog(
                resultSet.getString("log_id"),
                resultSet.getString("operator"),
                resultSet.getString("action"),
                resultSet.getString("target_type"),
                resultSet.getString("target_id"),
                resultSet.getString("detail"),
                toInstant(resultSet.getTimestamp("created_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
