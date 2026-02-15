-- Service URL configurations per environment
CREATE TABLE service_url_configs (
    id BINARY(16) PRIMARY KEY,
    environment VARCHAR(20) NOT NULL,
    service_key VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    url VARCHAR(512) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_env_service (environment, service_key),
    INDEX idx_suc_environment (environment),
    INDEX idx_suc_category (category),
    INDEX idx_suc_service_key (service_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Infrastructure configurations per environment
CREATE TABLE infrastructure_configs (
    id BINARY(16) PRIMARY KEY,
    environment VARCHAR(20) NOT NULL,
    infra_key VARCHAR(30) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    username VARCHAR(100),
    password_encrypted VARCHAR(512),
    connection_string VARCHAR(512),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_env_infra (environment, infra_key),
    INDEX idx_ic_environment (environment),
    INDEX idx_ic_infra_key (infra_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Firebase configurations per environment
CREATE TABLE firebase_configs (
    id BINARY(16) PRIMARY KEY,
    environment VARCHAR(20) NOT NULL UNIQUE,
    project_id VARCHAR(100),
    client_email VARCHAR(255),
    private_key_encrypted TEXT,
    storage_bucket VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BINARY(16),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fc_environment (environment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default service URLs for development environment
-- Spring Boot Services
INSERT INTO service_url_configs (id, environment, service_key, category, url, description) VALUES
(UUID_TO_BIN(UUID()), 'development', 'AUTH_SERVICE', 'SPRING', 'http://auth-service:8080', 'Authentication & authorization service'),
(UUID_TO_BIN(UUID()), 'development', 'USER_SERVICE', 'SPRING', 'http://user-service:8081', 'User management service'),
(UUID_TO_BIN(UUID()), 'development', 'PERMISSION_SERVICE', 'SPRING', 'http://permission-service:8082', 'Role-based permission service'),
(UUID_TO_BIN(UUID()), 'development', 'AUDIT_SERVICE', 'SPRING', 'http://audit-service:8083', 'Audit logging service'),
(UUID_TO_BIN(UUID()), 'development', 'ADMIN_SERVICE', 'SPRING', 'http://admin-service:8085', 'Administration service'),
(UUID_TO_BIN(UUID()), 'development', 'SECURITY_SERVICE', 'SPRING', 'http://security-service:8086', 'Threat detection & WAF service');

-- NestJS Services
INSERT INTO service_url_configs (id, environment, service_key, category, url, description) VALUES
(UUID_TO_BIN(UUID()), 'development', 'BACKEND_GATEWAY', 'NESTJS', 'http://backend-gateway:3000', 'API gateway'),
(UUID_TO_BIN(UUID()), 'development', 'NOTIFICATION_SERVICE', 'NESTJS', 'http://notification-service:3001', 'Push notification service'),
(UUID_TO_BIN(UUID()), 'development', 'REALTIME_SERVICE', 'NESTJS', 'http://realtime-service:3002', 'WebSocket & real-time events');

-- Elixir Services
INSERT INTO service_url_configs (id, environment, service_key, category, url, description) VALUES
(UUID_TO_BIN(UUID()), 'development', 'PRESENCE_SERVICE', 'ELIXIR', 'http://presence-service:4000', 'Online presence tracking'),
(UUID_TO_BIN(UUID()), 'development', 'MESSAGE_SERVICE', 'ELIXIR', 'http://message-service:4001', 'Message handling service'),
(UUID_TO_BIN(UUID()), 'development', 'CALL_SERVICE', 'ELIXIR', 'http://call-service:4002', 'Voice/video call service'),
(UUID_TO_BIN(UUID()), 'development', 'NOTIFICATION_ORCHESTRATOR', 'ELIXIR', 'http://notification-orchestrator:4003', 'Notification routing orchestrator'),
(UUID_TO_BIN(UUID()), 'development', 'HUDDLE_SERVICE', 'ELIXIR', 'http://huddle-service:4004', 'Audio huddle rooms'),
(UUID_TO_BIN(UUID()), 'development', 'EVENT_BROADCAST_SERVICE', 'ELIXIR', 'http://event-broadcast-service:4005', 'Event broadcasting service');

-- Go Services
INSERT INTO service_url_configs (id, environment, service_key, category, url, description) VALUES
(UUID_TO_BIN(UUID()), 'development', 'WORKSPACE_SERVICE', 'GO', 'http://workspace-service:8090', 'Workspace management'),
(UUID_TO_BIN(UUID()), 'development', 'CHANNEL_SERVICE', 'GO', 'http://channel-service:8091', 'Channel management'),
(UUID_TO_BIN(UUID()), 'development', 'THREAD_SERVICE', 'GO', 'http://thread-service:8092', 'Thread management'),
(UUID_TO_BIN(UUID()), 'development', 'SEARCH_SERVICE', 'GO', 'http://search-service:8093', 'Full-text search'),
(UUID_TO_BIN(UUID()), 'development', 'FILE_SERVICE', 'GO', 'http://file-service:8094', 'File management'),
(UUID_TO_BIN(UUID()), 'development', 'MEDIA_SERVICE', 'GO', 'http://media-service:8095', 'Media processing'),
(UUID_TO_BIN(UUID()), 'development', 'BOOKMARK_SERVICE', 'GO', 'http://bookmark-service:8096', 'Bookmark management'),
(UUID_TO_BIN(UUID()), 'development', 'REMINDER_SERVICE', 'GO', 'http://reminder-service:8097', 'Reminder scheduling'),
(UUID_TO_BIN(UUID()), 'development', 'ATTACHMENT_SERVICE', 'GO', 'http://attachment-service:8098', 'Attachment handling'),
(UUID_TO_BIN(UUID()), 'development', 'CDN_SERVICE', 'GO', 'http://cdn-service:8099', 'CDN & static assets');

-- Python Services
INSERT INTO service_url_configs (id, environment, service_key, category, url, description) VALUES
(UUID_TO_BIN(UUID()), 'development', 'ANALYTICS_SERVICE', 'PYTHON', 'http://analytics-service:5000', 'Analytics & metrics'),
(UUID_TO_BIN(UUID()), 'development', 'ML_SERVICE', 'PYTHON', 'http://ml-service:5001', 'Machine learning service'),
(UUID_TO_BIN(UUID()), 'development', 'MODERATION_SERVICE', 'PYTHON', 'http://moderation-service:5002', 'Content moderation'),
(UUID_TO_BIN(UUID()), 'development', 'EXPORT_SERVICE', 'PYTHON', 'http://export-service:5003', 'Data export service'),
(UUID_TO_BIN(UUID()), 'development', 'INTEGRATION_SERVICE', 'PYTHON', 'http://integration-service:5004', 'Third-party integrations'),
(UUID_TO_BIN(UUID()), 'development', 'SENTIMENT_SERVICE', 'PYTHON', 'http://sentiment-service:5005', 'Sentiment analysis'),
(UUID_TO_BIN(UUID()), 'development', 'INSIGHTS_SERVICE', 'PYTHON', 'http://insights-service:5006', 'Data insights'),
(UUID_TO_BIN(UUID()), 'development', 'SMART_REPLY_SERVICE', 'PYTHON', 'http://smart-reply-service:5007', 'AI smart replies');

-- Insert default infrastructure for development environment
INSERT INTO infrastructure_configs (id, environment, infra_key, host, port) VALUES
(UUID_TO_BIN(UUID()), 'development', 'REDIS', 'redis', 6379),
(UUID_TO_BIN(UUID()), 'development', 'MYSQL', 'mysql', 3306),
(UUID_TO_BIN(UUID()), 'development', 'POSTGRES', 'postgresql', 5432),
(UUID_TO_BIN(UUID()), 'development', 'MONGODB', 'mongodb', 27017),
(UUID_TO_BIN(UUID()), 'development', 'KAFKA', 'kafka', 9092),
(UUID_TO_BIN(UUID()), 'development', 'ELASTICSEARCH', 'elasticsearch', 9200),
(UUID_TO_BIN(UUID()), 'development', 'MINIO', 'minio', 9000);
