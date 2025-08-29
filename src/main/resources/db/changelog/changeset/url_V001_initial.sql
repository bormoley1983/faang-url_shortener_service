CREATE TABLE url
(
    hash       VARCHAR(6) PRIMARY KEY,
    url        VARCHAR(4096) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW()
)

CREATE TABLE hash
(
    hash VARCHAR(6) PRIMARY KEY,
)

CREATE SEQUENCE unique_hash_seq
    START WITH 916132832 -- to start hash generation from 6 chars
    INCREMENT BY 1
    NO MAXVALUE;