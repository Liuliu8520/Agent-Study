package com.agentstudy.agent.persistence;

import com.agentstudy.agent.AgentType;
import com.agentstudy.agent.PromptTemplate;
import com.agentstudy.agent.PromptTemplateRepository;
import com.agentstudy.learn.persistence.LearningPersistenceProperties;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class MySqlPromptTemplateRepository implements PromptTemplateRepository {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS prompt_template (
                code VARCHAR(100) NOT NULL PRIMARY KEY COMMENT 'prompt template code',
                agent_type VARCHAR(50) NOT NULL COMMENT 'agent type',
                version VARCHAR(20) NOT NULL COMMENT 'prompt version',
                name VARCHAR(100) NOT NULL COMMENT 'template display name',
                system_prompt TEXT NOT NULL COMMENT 'system prompt',
                user_prompt_template MEDIUMTEXT NOT NULL COMMENT 'user prompt template',
                created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created time',
                updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'updated time',
                KEY idx_prompt_template_agent_type (agent_type)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String UPSERT_SQL = """
            INSERT INTO prompt_template (
                code, agent_type, version, name, system_prompt, user_prompt_template
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                agent_type = VALUES(agent_type),
                version = VALUES(version),
                name = VALUES(name),
                system_prompt = VALUES(system_prompt),
                user_prompt_template = VALUES(user_prompt_template),
                updated_at = CURRENT_TIMESTAMP(6)
            """;

    private final LearningPersistenceProperties properties;

    public MySqlPromptTemplateRepository(LearningPersistenceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initializeSchema() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize prompt_template table", exception);
        }
    }

    @Override
    public List<PromptTemplate> findAll() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                             SELECT code, agent_type, version, name, system_prompt, user_prompt_template
                             FROM prompt_template
                             ORDER BY code
                             """
             );
             ResultSet resultSet = statement.executeQuery()) {
            List<PromptTemplate> templates = new ArrayList<>();
            while (resultSet.next()) {
                templates.add(mapRow(resultSet));
            }
            return templates;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list prompt templates", exception);
        }
    }

    @Override
    public Optional<PromptTemplate> findByCode(String code) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                             SELECT code, agent_type, version, name, system_prompt, user_prompt_template
                             FROM prompt_template
                             WHERE code = ?
                             """
             )) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find prompt template: " + code, exception);
        }
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, template.code());
            statement.setString(2, template.agentType().name());
            statement.setString(3, template.version());
            statement.setString(4, template.name());
            statement.setString(5, template.systemPrompt());
            statement.setString(6, template.userPromptTemplate());
            statement.executeUpdate();
            return template;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save prompt template: " + template.code(), exception);
        }
    }

    private PromptTemplate mapRow(ResultSet rs) throws SQLException {
        return new PromptTemplate(
                rs.getString("code"),
                AgentType.valueOf(rs.getString("agent_type")),
                rs.getString("version"),
                rs.getString("name"),
                rs.getString("system_prompt"),
                rs.getString("user_prompt_template")
        );
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
