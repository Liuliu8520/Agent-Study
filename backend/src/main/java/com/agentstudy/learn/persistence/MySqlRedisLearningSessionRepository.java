package com.agentstudy.learn.persistence;

import com.agentstudy.learn.LearningSessionRepository;
import com.agentstudy.learn.LearningState;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Profile("dev")
public class MySqlRedisLearningSessionRepository implements LearningSessionRepository {

    private static final Logger log = LoggerFactory.getLogger(MySqlRedisLearningSessionRepository.class);
    private static final String CACHE_KEY_PREFIX = "agent-study:learning-session:";

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS learning_session (
                session_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'learning session id',
                student_name VARCHAR(100) NOT NULL COMMENT 'student display name',
                current_step INT NOT NULL COMMENT 'current learning step',
                status VARCHAR(32) NOT NULL COMMENT 'session status',
                state_json JSON NOT NULL COMMENT 'serialized LearningState snapshot',
                created_at DATETIME(6) NOT NULL COMMENT 'created time',
                updated_at DATETIME(6) NOT NULL COMMENT 'last updated time',
                KEY idx_learning_session_student (student_name),
                KEY idx_learning_session_status_updated (status, updated_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String UPSERT_SQL = """
            INSERT INTO learning_session (
                session_id, student_name, current_step, status, state_json, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                student_name = VALUES(student_name),
                current_step = VALUES(current_step),
                status = VALUES(status),
                state_json = VALUES(state_json),
                updated_at = VALUES(updated_at)
            """;

    private final LearningPersistenceProperties properties;
    private final LearningStateJsonCodec jsonCodec;
    private final StringRedisTemplate redisTemplate;

    public MySqlRedisLearningSessionRepository(
            LearningPersistenceProperties properties,
            LearningStateJsonCodec jsonCodec,
            StringRedisTemplate redisTemplate
    ) {
        this.properties = properties;
        this.jsonCodec = jsonCodec;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize learning_session table", exception);
        }
    }

    @Override
    public LearningState save(LearningState state) {
        String stateJson = jsonCodec.encode(state);
        saveToMysql(state, stateJson);
        cache(state.getSessionId(), stateJson);
        return state;
    }

    @Override
    public Optional<LearningState> findById(String sessionId) {
        Optional<LearningState> cachedState = findFromCache(sessionId);
        if (cachedState.isPresent()) {
            return cachedState;
        }

        Optional<String> stateJson = findJsonFromMysql(sessionId);
        stateJson.ifPresent(json -> cache(sessionId, json));
        return stateJson.map(jsonCodec::decode);
    }

    @Override
    public List<LearningState> findAll() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT state_json FROM learning_session ORDER BY updated_at DESC"
             );
             ResultSet resultSet = statement.executeQuery()) {
            List<LearningState> states = new ArrayList<>();
            while (resultSet.next()) {
                states.add(jsonCodec.decode(resultSet.getString("state_json")));
            }
            return states;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list learning sessions", exception);
        }
    }

    @Override
    public int count() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM learning_session");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to count learning sessions", exception);
        }
    }

    private void saveToMysql(LearningState state, String stateJson) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, state.getSessionId());
            statement.setString(2, state.getStudentName());
            statement.setInt(3, state.getCurrentStep());
            statement.setString(4, state.getStatus().name());
            statement.setString(5, stateJson);
            statement.setTimestamp(6, Timestamp.from(state.getCreatedAt()));
            statement.setTimestamp(7, Timestamp.from(state.getUpdatedAt()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save learning session: " + state.getSessionId(), exception);
        }
    }

    private Optional<String> findJsonFromMysql(String sessionId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT state_json FROM learning_session WHERE session_id = ?"
             )) {
            statement.setString(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet.getString("state_json"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find learning session: " + sessionId, exception);
        }
    }

    private Optional<LearningState> findFromCache(String sessionId) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey(sessionId));
            if (!StringUtils.hasText(json)) {
                return Optional.empty();
            }
            return Optional.of(jsonCodec.decode(json));
        } catch (DataAccessException exception) {
            log.warn("Redis cache read failed, fallback to MySQL. sessionId={}", sessionId, exception);
            return Optional.empty();
        }
    }

    private void cache(String sessionId, String stateJson) {
        try {
            int ttlMinutes = properties.getCacheTtlMinutes();
            if (ttlMinutes > 0) {
                redisTemplate.opsForValue().set(cacheKey(sessionId), stateJson, Duration.ofMinutes(ttlMinutes));
            } else {
                redisTemplate.opsForValue().set(cacheKey(sessionId), stateJson);
            }
        } catch (DataAccessException exception) {
            log.warn("Redis cache write failed, session still saved in MySQL. sessionId={}", sessionId, exception);
        }
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }

    private String cacheKey(String sessionId) {
        return CACHE_KEY_PREFIX + sessionId;
    }
}
