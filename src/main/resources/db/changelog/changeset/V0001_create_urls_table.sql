CREATE TABLE urls (
    hash VARCHAR(6) PRIMARY KEY,
    original_url TEXT NOT NULL,
    expires_at timestamptz NULL,
    created_at timestamptz DEFAULT current_timestamp
);