package com.agentstudy.rag.persistence;

import com.agentstudy.learn.persistence.LearningPersistenceProperties;
import com.agentstudy.rag.InMemoryKnowledgeBase;
import com.agentstudy.rag.KnowledgeChunk;
import com.agentstudy.rag.KnowledgeChunkRepository;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class MySqlKnowledgeChunkRepository implements KnowledgeChunkRepository {

    private static final TypeReference<List<String>> TAG_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Double>> EMBEDDING_LIST_TYPE = new TypeReference<>() {
    };

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS knowledge_chunk (
                chunk_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'knowledge chunk id',
                chapter VARCHAR(100) NOT NULL COMMENT 'chapter name',
                title VARCHAR(200) NOT NULL COMMENT 'chunk title',
                content TEXT NOT NULL COMMENT 'chunk content',
                tags_json JSON NOT NULL COMMENT 'retrieval tags',
                embedding_json JSON NULL COMMENT 'embedding vector',
                enabled TINYINT NOT NULL DEFAULT 1 COMMENT 'whether chunk is enabled',
                created_at DATETIME(6) NOT NULL COMMENT 'created time',
                updated_at DATETIME(6) NOT NULL COMMENT 'updated time',
                KEY idx_knowledge_chunk_chapter (chapter),
                KEY idx_knowledge_chunk_enabled_updated (enabled, updated_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    private static final String UPSERT_SQL = """
            INSERT INTO knowledge_chunk (
                chunk_id, chapter, title, content, tags_json, embedding_json, enabled, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?)
            ON DUPLICATE KEY UPDATE
                chapter = VALUES(chapter),
                title = VALUES(title),
                content = VALUES(content),
                tags_json = VALUES(tags_json),
                embedding_json = VALUES(embedding_json),
                enabled = VALUES(enabled),
                updated_at = VALUES(updated_at)
            """;

    private final LearningPersistenceProperties properties;
    private final ObjectMapper objectMapper;
    private final InMemoryKnowledgeBase defaultKnowledgeBase;

    public MySqlKnowledgeChunkRepository(
            LearningPersistenceProperties properties,
            ObjectMapper objectMapper,
            InMemoryKnowledgeBase defaultKnowledgeBase
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.defaultKnowledgeBase = defaultKnowledgeBase;
    }

    @PostConstruct
    void initializeSchemaAndSeedData() {
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
                statement.executeUpdate();
            }
            ensureEmbeddingColumn(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize knowledge_chunk table", exception);
        }

        if (count() == 0) {
            defaultKnowledgeBase.listChunks().forEach(this::save);
        }
    }

    @Override
    public KnowledgeChunk save(KnowledgeChunk chunk) {
        Instant now = Instant.now();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setString(1, chunk.id());
            statement.setString(2, chunk.chapter());
            statement.setString(3, chunk.title());
            statement.setString(4, chunk.content());
            statement.setString(5, toJson(chunk.tags()));
            statement.setString(6, chunk.embedding().isEmpty() ? null : toJson(chunk.embedding()));
            statement.setTimestamp(7, Timestamp.from(now));
            statement.setTimestamp(8, Timestamp.from(now));
            statement.executeUpdate();
            return chunk;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save knowledge chunk: " + chunk.id(), exception);
        }
    }

    @Override
    public Optional<KnowledgeChunk> findById(String id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM knowledge_chunk WHERE chunk_id = ? AND enabled = 1"
             )) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find knowledge chunk: " + id, exception);
        }
    }

    @Override
    public List<KnowledgeChunk> findAll() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM knowledge_chunk WHERE enabled = 1 ORDER BY chunk_id"
             );
             ResultSet resultSet = statement.executeQuery()) {
            List<KnowledgeChunk> chunks = new ArrayList<>();
            while (resultSet.next()) {
                chunks.add(mapRow(resultSet));
            }
            return chunks;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list knowledge chunks", exception);
        }
    }

    @Override
    public void deleteById(String id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE knowledge_chunk SET enabled = 0, updated_at = CURRENT_TIMESTAMP(6) WHERE chunk_id = ?"
             )) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete knowledge chunk: " + id, exception);
        }
    }

    @Override
    public int count() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM knowledge_chunk WHERE enabled = 1");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to count knowledge chunks", exception);
        }
    }

    private KnowledgeChunk mapRow(ResultSet resultSet) throws SQLException {
        return new KnowledgeChunk(
                resultSet.getString("chunk_id"),
                resultSet.getString("chapter"),
                resultSet.getString("title"),
                resultSet.getString("content"),
                fromJson(resultSet.getString("tags_json")),
                fromEmbeddingJson(resultSet.getString("embedding_json"))
        );
    }

    private void ensureEmbeddingColumn(Connection connection) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, "knowledge_chunk", "embedding_json")) {
            if (columns.next()) {
                return;
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "ALTER TABLE knowledge_chunk ADD COLUMN embedding_json JSON NULL COMMENT 'embedding vector' AFTER tags_json"
        )) {
            statement.executeUpdate();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize knowledge chunk JSON field", exception);
        }
    }

    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, TAG_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize knowledge chunk tags", exception);
        }
    }

    private List<Double> fromEmbeddingJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, EMBEDDING_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize knowledge chunk embedding", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        LearningPersistenceProperties.Mysql mysql = properties.getMysql();
        return DriverManager.getConnection(mysql.getUrl(), mysql.getUsername(), mysql.getPassword());
    }
}
