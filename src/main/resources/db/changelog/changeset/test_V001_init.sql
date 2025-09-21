CREATE SEQUENCE IF NOT EXISTS unique_numbers_seq
START WITH 1
INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS hashes
(
    hash        VARCHAR(6) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS urls
(
    hash                VARCHAR(6) PRIMARY KEY,
    url                 TEXT UNIQUE NOT NULL,
    request_count       BIGINT NOT NULL DEFAULT 1,
    created_at          timestamptz DEFAULT current_timestamp,
    last_requested_at   timestamptz DEFAULT current_timestamp
);