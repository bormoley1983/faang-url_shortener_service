CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(7) NOT NULL,
    original_url TEXT NOT NULL,
    expires_at timestamptz NULL,
    created_at timestamptz DEFAULT current_timestamp
);

CREATE UNIQUE INDEX idx_urls_hash ON urls(hash);