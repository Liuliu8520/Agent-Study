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
