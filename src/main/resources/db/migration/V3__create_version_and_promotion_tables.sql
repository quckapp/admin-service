-- V3: Version management tables + Promotion records

CREATE TABLE IF NOT EXISTS version_configs (
    id BINARY(16) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    service_key VARCHAR(50) NOT NULL,
    api_version VARCHAR(20) NOT NULL,
    release_version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    sunset_date DATE,
    sunset_duration_days INT,
    deprecated_at DATETIME(6),
    changelog TEXT,
    updated_by VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_env_service_version UNIQUE (environment, service_key, api_version),
    INDEX idx_vc_environment (environment),
    INDEX idx_vc_service_key (service_key),
    INDEX idx_vc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS global_version_configs (
    id BINARY(16) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    default_api_version VARCHAR(20) NOT NULL DEFAULT 'v1',
    default_sunset_days INT NOT NULL DEFAULT 90,
    updated_by VARCHAR(255),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_gvc_environment UNIQUE (environment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS version_profiles (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS version_profile_entries (
    id BINARY(16) NOT NULL,
    profile_id BINARY(16) NOT NULL,
    service_key VARCHAR(50) NOT NULL,
    api_version VARCHAR(20) NOT NULL,
    release_version VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT fk_vpe_profile FOREIGN KEY (profile_id) REFERENCES version_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promotion_records (
    id BINARY(16) NOT NULL,
    version_config_id BINARY(16) NOT NULL,
    service_key VARCHAR(50) NOT NULL,
    api_version VARCHAR(20) NOT NULL,
    from_environment VARCHAR(20) NOT NULL,
    to_environment VARCHAR(20) NOT NULL,
    promotion_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    promoted_by VARCHAR(255) NOT NULL,
    approver1 VARCHAR(255),
    approver2 VARCHAR(255),
    jira_ticket VARCHAR(50),
    reason TEXT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pr_version FOREIGN KEY (version_config_id) REFERENCES version_configs(id),
    INDEX idx_pr_service (service_key, api_version),
    INDEX idx_pr_environment (to_environment),
    INDEX idx_pr_type (promotion_type),
    INDEX idx_pr_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
