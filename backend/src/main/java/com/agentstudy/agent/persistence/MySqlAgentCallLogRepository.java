package com.agentstudy.agent.persistence;

import com.agentstudy.agent.AgentCallLog;
import com.agentstudy.agent.AgentCallLogRepository;
import com.agentstudy.agent.AgentCallStatus;
import com.agentstudy.agent.AgentType;
import com.agentstudy.learn.persistence.LearningPersistenceProperties;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class MySqlAgentCallLogRepository implements AgentCallLogRepository {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS agent_call_log (
                call_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'agent call id',
                session_id VARCHAR(64) NULL COMMENT 'learning session id',
                agent_type VARCHAR(50) NOT NULL COMMENT 'agent type',
                prompt_code VARCHAR(100) NOT NULL COMMENT 'prompt template code',
                prompt_version VARCHAR(20) NOT NULL COMMENT 'prompt template version',
                model_name VARCHAR(100) NOT NULL COMMENT 'model name',
                request_payload MEDIUMTEXT NOT NULL COMMENT 'rendered prompt payload',
                response_text MEDIUMTEXT NULL COMMENT 'llm response text',
                status VARCHAR(32) NOT NULL COMMENT 'call status',
                error_message VARCHAR(1000) NULL COMMENT 'error message',
                duration_millis BIGINT NOT NULL COMMENT 'call duration in milliseconds',
                created_at DATETIME(6) NOT NULL COMMENT 'created time',
                KEY idx_agent_call_log_session_created (session_id, created_at),
                KEY idx_agent_call_log_agent_created (agent_type, created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String INSERT_SQL = """
            INSERT INTO agent_call_log (
                call_id, session_id, agent_type, prompt_code, prompt_version, model_name,
                request_payload, response_text, status, error_message, duration_millis, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final LearningPersistenceProperties properties;

    public MySqlAgentCallLogRepository(LearningPersistenceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize agent_call_log table", exception);
        }
    }

    @Override
    public AgentCallLog save(AgentCallLog log) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, log.callId());
            statement.setString(2, log.sessionId());
            statement.setString(3, log.agentType().name());
            statement.setString(4, log.promptCode());
            statement.setString(5, log.promptVersion());
            statement.setString(6, log.modelName());
            statement.setString(7, log.requestPayload());
            statement.setString(8, log.responseText());
            statement.setString(9, log.status().name());
            statement.setString(10, log.errorMessage());
            statement.setLong(11, log.durationMillis());
            statement.setTimestamp(12, Timestamp.from(log.createdAt()));
            statement.executeUpdate();
            return log;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save agent call log: " + log.callId(), exception);
        }
    }

    @Override
    public Optional<AgentCallLog> findById(String callId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM agent_call_log WHERE call_id = ?"
             )) {
            statement.setString(1, callId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find agent call log: " + callId, exception);
        }
    }

    @Override
    public List<AgentCallLog> findLatest(int limit) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM agent_call_log ORDER BY created_at DESC LIMIT ?"
             )) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AgentCallLog> logs = new ArrayList<>();
                while (resultSet.next()) {
                    logs.add(mapRow(resultSet));
                }
                return logs;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list agent call logs", exception);
        }
    }

    private AgentCallLog mapRow(ResultSet resultSet) throws SQLException {
        return new AgentCallLog(
                resultSet.getString("call_id"),
                resultSet.getString("session_id"),
                AgentType.valueOf(resultSet.getString("agent_type")),
                resultSet.getString("prompt_code"),
                resultSet.getString("prompt_version"),
                resultSet.getString("model_name"),
                resultSet.getString("request_payload"),
                resultSet.getString("response_text"),
                AgentCallStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("error_message"),
                resultSet.getLong("duration_millis"),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
