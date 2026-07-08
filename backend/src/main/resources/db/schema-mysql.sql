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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agent_call_log (
    call_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'agent call id',
    session_id VARCHAR(64) NULL COMMENT 'learning session id',
    agent_type VARCHAR(50) NOT NULL COMMENT 'agent type',
    prompt_code VARCHAR(100) NOT NULL COMMENT 'prompt template code',
    prompt_version VARCHAR(20) NOT NULL COMMENT 'prompt template version',
    model_name VARCHAR(100) NOT NULL COMMENT 'model name',
    request_payload MEDIUMTEXT NOT NULL COMMENT 'rendered prompt payload',
    response_text MEDIUMTEXT NULL COMMENT 'llm response text',
    prompt_tokens INT NOT NULL DEFAULT 0 COMMENT 'prompt token count',
    completion_tokens INT NOT NULL DEFAULT 0 COMMENT 'completion token count',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT 'total token count',
    status VARCHAR(32) NOT NULL COMMENT 'call status',
    error_message VARCHAR(1000) NULL COMMENT 'error message',
    duration_millis BIGINT NOT NULL COMMENT 'call duration in milliseconds',
    created_at DATETIME(6) NOT NULL COMMENT 'created time',
    KEY idx_agent_call_log_session_created (session_id, created_at),
    KEY idx_agent_call_log_agent_created (agent_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exercise_attempt (
    attempt_id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'exercise attempt id',
    session_id VARCHAR(64) NOT NULL COMMENT 'learning session id',
    correct_count INT NOT NULL COMMENT 'correct answer count',
    total_count INT NOT NULL COMMENT 'total exercise count',
    error_rate DOUBLE NOT NULL COMMENT 'exercise error rate',
    results_json JSON NOT NULL COMMENT 'exercise judge results',
    submitted_at DATETIME(6) NOT NULL COMMENT 'submitted time',
    KEY idx_exercise_attempt_session_submitted (session_id, submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
