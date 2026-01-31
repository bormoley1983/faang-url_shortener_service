CREATE TABLE IF NOT EXISTS url (
    hash varchar(6) NOT NULL PRIMARY KEY CHECK (char_length(hash)=6),
    url varchar(2048) NOT NULL,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamptz DEFAULT CURRENT_TIMESTAMP + '1 year'::interval
);

CREATE TABLE IF NOT EXISTS hash (
    hash varchar(6) NOT NULL PRIMARY KEY CHECK (char_length(hash)=6)
);

CREATE SEQUENCE IF NOT EXISTS unique_number_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;