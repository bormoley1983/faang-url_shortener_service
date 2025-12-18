CREATE TABLE analytics (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES urls(id),
    clicked_at timestamptz DEFAULT current_timestamp,
    user_agent TEXT,
    ip_address TEXT
);
