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
    status VARCHAR(32) NOT NULL COMMENT 'call status',
    error_message VARCHAR(1000) NULL COMMENT 'error message',
    duration_millis BIGINT NOT NULL COMMENT 'call duration in milliseconds',
    created_at DATETIME(6) NOT NULL COMMENT 'created time',
    KEY idx_agent_call_log_session_created (session_id, created_at),
    KEY idx_agent_call_log_agent_created (agent_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
