CREATE SEQUENCE unique_hash_number_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE hash
(
    id BIGINT PRIMARY KEY,
    hash VARCHAR NOT NULL
);

CREATE TABLE url
(
    id BIGSERIAL PRIMARY KEY,
    hash       VARCHAR(6) UNIQUE NOT NULL,
    long_url   TEXT NOT NULL,
    created_at timestamptz NOT NULL DEFAULT NOW()
);

