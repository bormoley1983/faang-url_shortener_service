-- Таблица для хранения свободных и используемых хэшей
CREATE TABLE hashes (
    id BIGSERIAL PRIMARY KEY,
    hash_value VARCHAR(10) NOT NULL UNIQUE,
    is_used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Таблица для хранения связей между коротким и оригинальным URL
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(10) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    access_count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_hash FOREIGN KEY (hash) REFERENCES hashes(hash_value)
);

-- Индексы для оптимизации поиска
CREATE INDEX idx_urls_hash ON urls(hash);
CREATE INDEX idx_urls_created_at ON urls(created_at);
CREATE INDEX idx_urls_access_count ON urls(access_count DESC);


-- Индексы для оптимизации поиска
CREATE INDEX idx_hashes_hash_value ON hashes(hash_value);
CREATE INDEX idx_hashes_is_used ON hashes(is_used);