CREATE SEQUENCE unique_number_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE hash
(
    hash varchar(6) PRIMARY KEY
);

CREATE TABLE url
(
    hash       varchar(6) PRIMARY KEY,
    url        TEXT        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT current_timestamp
);
