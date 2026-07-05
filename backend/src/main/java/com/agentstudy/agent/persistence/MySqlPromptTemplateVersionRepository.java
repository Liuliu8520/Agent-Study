package com.agentstudy.agent.persistence;

import com.agentstudy.agent.AgentType;
import com.agentstudy.agent.PromptTemplate;
import com.agentstudy.agent.PromptTemplateVersion;
import com.agentstudy.agent.PromptTemplateVersionRepository;
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
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class MySqlPromptTemplateVersionRepository implements PromptTemplateVersionRepository {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS prompt_template_version (
                version_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'prompt version id',
                code VARCHAR(100) NOT NULL COMMENT 'prompt template code',
                agent_type VARCHAR(50) NOT NULL COMMENT 'agent type',
                version_label VARCHAR(20) NOT NULL COMMENT 'business version label',
                name VARCHAR(100) NOT NULL COMMENT 'template display name',
                system_prompt TEXT NOT NULL COMMENT 'system prompt',
                user_prompt_template MEDIUMTEXT NOT NULL COMMENT 'user prompt template',
                active TINYINT NOT NULL DEFAULT 0 COMMENT 'whether this version is active',
                created_by VARCHAR(100) NOT NULL COMMENT 'operator username',
                created_at DATETIME(6) NOT NULL COMMENT 'created time',
                KEY idx_prompt_template_version_code_created (code, created_at),
                KEY idx_prompt_template_version_code_active (code, active)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String INSERT_SQL = """
            INSERT INTO prompt_template_version (
                version_id, code, agent_type, version_label, name, system_prompt,
                user_prompt_template, active, created_by, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final LearningPersistenceProperties properties;

    public MySqlPromptTemplateVersionRepository(LearningPersistenceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize prompt_template_version table", exception);
        }
    }

    @Override
    public PromptTemplateVersion saveVersion(PromptTemplate template, String createdBy, boolean active) {
        PromptTemplateVersion version = new PromptTemplateVersion(
                UUID.randomUUID().toString(),
                template.code(),
                template.agentType(),
                template.version(),
                template.name(),
                template.systemPrompt(),
                template.userPromptTemplate(),
                active,
                createdBy,
                Instant.now()
        );

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            if (active) {
                deactivateByCode(connection, template.code());
            }
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
                statement.setString(1, version.versionId());
                statement.setString(2, version.code());
                statement.setString(3, version.agentType().name());
                statement.setString(4, version.version());
                statement.setString(5, version.name());
                statement.setString(6, version.systemPrompt());
                statement.setString(7, version.userPromptTemplate());
                statement.setBoolean(8, version.active());
                statement.setString(9, version.createdBy());
                statement.setTimestamp(10, Timestamp.from(version.createdAt()));
                statement.executeUpdate();
            }
            connection.commit();
            return version;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save prompt template version: " + template.code(), exception);
        }
    }

    @Override
    public List<PromptTemplateVersion> findByCode(String code) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                             SELECT version_id, code, agent_type, version_label, name, system_prompt,
                                    user_prompt_template, active, created_by, created_at
                             FROM prompt_template_version
                             WHERE code = ?
                             ORDER BY created_at DESC
                             """
             )) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PromptTemplateVersion> versions = new ArrayList<>();
                while (resultSet.next()) {
                    versions.add(mapRow(resultSet));
                }
                return versions;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list prompt template versions: " + code, exception);
        }
    }

    @Override
    public Optional<PromptTemplateVersion> findById(String versionId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                             SELECT version_id, code, agent_type, version_label, name, system_prompt,
                                    user_prompt_template, active, created_by, created_at
                             FROM prompt_template_version
                             WHERE version_id = ?
                             """
             )) {
            statement.setString(1, versionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find prompt template version: " + versionId, exception);
        }
    }

    @Override
    public void activate(String code, String versionId) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            deactivateByCode(connection, code);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE prompt_template_version SET active = 1 WHERE code = ? AND version_id = ?"
            )) {
                statement.setString(1, code);
                statement.setString(2, versionId);
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to activate prompt template version: " + versionId, exception);
        }
    }

    private void deactivateByCode(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE prompt_template_version SET active = 0 WHERE code = ?"
        )) {
            statement.setString(1, code);
            statement.executeUpdate();
        }
    }

    private PromptTemplateVersion mapRow(ResultSet resultSet) throws SQLException {
        return new PromptTemplateVersion(
                resultSet.getString("version_id"),
                resultSet.getString("code"),
                AgentType.valueOf(resultSet.getString("agent_type")),
                resultSet.getString("version_label"),
                resultSet.getString("name"),
                resultSet.getString("system_prompt"),
                resultSet.getString("user_prompt_template"),
                resultSet.getBoolean("active"),
                resultSet.getString("created_by"),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
