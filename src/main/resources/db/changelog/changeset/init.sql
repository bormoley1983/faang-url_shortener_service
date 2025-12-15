CREATE SEQUENCE unique_hash_number_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE hash
(
    id BIGINT PRIMARY KEY,
    hash VARCHAR NOT NULL, --TODO подумать, нужно ли VARCHAR(6)
    long_url TEXT NOT NULL
);

CREATE TABLE url
(
    hash       VARCHAR(6) PRIMARY KEY,
    url        TEXT        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT NOW()
);

