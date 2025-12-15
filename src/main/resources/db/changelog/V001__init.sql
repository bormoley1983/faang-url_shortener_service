--liquibase formatted sql

--changeset kirill_mir:create_unique_number_seq_20251215
CREATE SEQUENCE unique_number_seq
    START WITH 916132832
    INCREMENT BY 1
    NO MAXVALUE;

--changeset kirill_mir:create_free_hash_storage_20251215
CREATE TABLE free_hash_storage (
    hash VARCHAR(6) PRIMARY KEY,

    CHECK (char_length(hash) = 6)
);

--changeset kirill_mir:create_short_url_table_20251215
CREATE TABLE url (
    hash VARCHAR(6) PRIMARY KEY,
    actual_url TEXT NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (char_length(hash) = 6),
    CHECK (expire_time > created_at)
);