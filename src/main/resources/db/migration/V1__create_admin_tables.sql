-- System settings table
CREATE TABLE system_settings (
    id BINARY(16) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255),
    encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_settings_category (category),
    INDEX idx_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Workspace settings table
CREATE TABLE workspace_settings (
    id BINARY(16) PRIMARY KEY,
    workspace_id BINARY(16) NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    description VARCHAR(255),
    updated_by BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ws_settings (workspace_id, setting_key),
    INDEX idx_ws_settings_workspace (workspace_id),
    INDEX idx_ws_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Feature flags table
CREATE TABLE feature_flags (
    id BINARY(16) PRIMARY KEY,
    feature_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    target_rules JSON,
    rollout_percentage INT NOT NULL DEFAULT 0,
    workspace_id BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feature_key (feature_key),
    INDEX idx_feature_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Maintenance windows table
CREATE TABLE maintenance_windows (
    id BINARY(16) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    type ENUM('PLANNED', 'EMERGENCY', 'UPGRADE', 'SECURITY_PATCH') NOT NULL,
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    affected_services JSON,
    created_by BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_maintenance_start (start_time),
    INDEX idx_maintenance_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default system settings
INSERT INTO system_settings (id, category, setting_key, setting_value, description, encrypted, editable) VALUES
(UUID_TO_BIN(UUID()), 'general', 'app.name', 'QuikApp', 'Application name', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'general', 'app.version', '1.0.0', 'Application version', FALSE, FALSE),
(UUID_TO_BIN(UUID()), 'general', 'app.environment', 'development', 'Current environment', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'security', 'security.session.timeout', '3600', 'Session timeout in seconds', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'security', 'security.max.login.attempts', '5', 'Maximum login attempts before lockout', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'security', 'security.lockout.duration', '900', 'Account lockout duration in seconds', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'limits', 'limits.max.file.size', '10485760', 'Maximum file upload size in bytes', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'limits', 'limits.max.message.length', '40000', 'Maximum message length', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'limits', 'limits.max.workspace.members', '1000', 'Maximum members per workspace', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'notifications', 'notifications.email.enabled', 'true', 'Enable email notifications', FALSE, TRUE),
(UUID_TO_BIN(UUID()), 'notifications', 'notifications.push.enabled', 'true', 'Enable push notifications', FALSE, TRUE);

-- Insert default feature flags
INSERT INTO feature_flags (id, feature_key, name, description, enabled, rollout_percentage) VALUES
(UUID_TO_BIN(UUID()), 'huddles', 'Audio Huddles', 'Enable audio huddle rooms', TRUE, 100),
(UUID_TO_BIN(UUID()), 'threads', 'Message Threads', 'Enable threaded replies', TRUE, 100),
(UUID_TO_BIN(UUID()), 'reactions', 'Message Reactions', 'Enable emoji reactions', TRUE, 100),
(UUID_TO_BIN(UUID()), 'ai_smart_reply', 'AI Smart Reply', 'Enable AI-powered smart reply suggestions', FALSE, 0),
(UUID_TO_BIN(UUID()), 'ai_summarization', 'AI Summarization', 'Enable AI-powered message summarization', FALSE, 0),
(UUID_TO_BIN(UUID()), 'advanced_search', 'Advanced Search', 'Enable advanced search capabilities', TRUE, 100),
(UUID_TO_BIN(UUID()), 'screen_share', 'Screen Sharing', 'Enable screen sharing in huddles', FALSE, 50);
