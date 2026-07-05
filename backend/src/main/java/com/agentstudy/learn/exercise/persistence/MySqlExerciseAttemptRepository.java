package com.agentstudy.learn.exercise.persistence;

import com.agentstudy.learn.exercise.ExerciseAttempt;
import com.agentstudy.learn.exercise.ExerciseAttemptRepository;
import com.agentstudy.learn.exercise.ExerciseJudgeResult;
import com.agentstudy.learn.persistence.LearningPersistenceProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class MySqlExerciseAttemptRepository implements ExerciseAttemptRepository {

    private static final TypeReference<List<ExerciseJudgeResult>> RESULT_LIST_TYPE = new TypeReference<>() {
    };

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS exercise_attempt (
                attempt_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'exercise attempt id',
                session_id VARCHAR(64) NOT NULL COMMENT 'learning session id',
                correct_count INT NOT NULL COMMENT 'correct answer count',
                total_count INT NOT NULL COMMENT 'total exercise count',
                error_rate DOUBLE NOT NULL COMMENT 'exercise error rate',
                results_json JSON NOT NULL COMMENT 'exercise judge results',
                submitted_at DATETIME(6) NOT NULL COMMENT 'submitted time',
                KEY idx_exercise_attempt_session_submitted (session_id, submitted_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String INSERT_SQL = """
            INSERT INTO exercise_attempt (
                attempt_id, session_id, correct_count, total_count, error_rate, results_json, submitted_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final LearningPersistenceProperties properties;
    private final ObjectMapper objectMapper;

    public MySqlExerciseAttemptRepository(
            LearningPersistenceProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize exercise_attempt table", exception);
        }
    }

    @Override
    public ExerciseAttempt save(ExerciseAttempt attempt) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, attempt.attemptId());
            statement.setString(2, attempt.sessionId());
            statement.setInt(3, attempt.correctCount());
            statement.setInt(4, attempt.totalCount());
            statement.setDouble(5, attempt.errorRate());
            statement.setString(6, toJson(attempt.results()));
            statement.setTimestamp(7, Timestamp.from(attempt.submittedAt()));
            statement.executeUpdate();
            return attempt;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save exercise attempt: " + attempt.attemptId(), exception);
        }
    }

    @Override
    public List<ExerciseAttempt> findBySessionId(String sessionId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM exercise_attempt WHERE session_id = ? ORDER BY submitted_at DESC"
             )) {
            statement.setString(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ExerciseAttempt> attempts = new ArrayList<>();
                while (resultSet.next()) {
                    attempts.add(mapRow(resultSet));
                }
                return attempts;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list exercise attempts for session: " + sessionId, exception);
        }
    }

    private ExerciseAttempt mapRow(ResultSet resultSet) throws SQLException {
        return new ExerciseAttempt(
                resultSet.getString("attempt_id"),
                resultSet.getString("session_id"),
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count"),
                resultSet.getDouble("error_rate"),
                fromJson(resultSet.getString("results_json")),
                resultSet.getTimestamp("submitted_at").toInstant()
        );
    }

    private String toJson(List<ExerciseJudgeResult> results) {
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize exercise attempt results", exception);
        }
    }

    private List<ExerciseJudgeResult> fromJson(String json) {
        try {
            return objectMapper.readValue(json, RESULT_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize exercise attempt results", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
