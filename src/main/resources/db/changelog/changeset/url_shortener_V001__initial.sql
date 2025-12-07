CREATE TABLE IF NOT EXISTS url (
    hash            varchar(6) PRIMARY KEY,
    url             TEXT NOT NULL,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hash (
    hash            varchar(6) NOT NULL PRIMARY KEY
);

CREATE SEQUENCE IF NOT EXISTS unique_number_seq
    START WITH 1
    INCREMENT BY 1;