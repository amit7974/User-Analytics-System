-- =========================================================
-- schema.sql - User Analytics + Semantic Search Backend
-- Run this against MySQL 8+ before starting the application
-- (the app uses ddl-auto: validate, so the schema must pre-exist).
-- =========================================================

CREATE DATABASE IF NOT EXISTS user_analytics
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_analytics;

-- -----------------------------------------------------------
-- Table: users
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     VARCHAR(100) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    CONSTRAINT uq_users_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------
-- Table: events
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       VARCHAR(100) NOT NULL,
    event         VARCHAR(255) NOT NULL,
    metadata      JSON         NULL,
    `timestamp`   DATETIME(6)  NOT NULL,
    embedding_id  BIGINT       NULL,
    created_at    DATETIME(6)  NOT NULL,
    INDEX idx_events_user_id (user_id),
    INDEX idx_events_event (event),
    INDEX idx_events_timestamp (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------
-- Table: embeddings
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS embeddings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id    BIGINT       NOT NULL,
    vector      JSON         NOT NULL,
    model_name  VARCHAR(100) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    INDEX idx_embeddings_event_id (event_id),
    CONSTRAINT fk_embeddings_event FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional FK from events -> embeddings (added after both tables exist)
ALTER TABLE events
    ADD CONSTRAINT fk_events_embedding FOREIGN KEY (embedding_id) REFERENCES embeddings(id)
        ON DELETE SET NULL;
