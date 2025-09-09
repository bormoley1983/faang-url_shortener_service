CREATE SEQUENCE unique_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS url (
    hash varchar(6) NOT NULL PRIMARY KEY,
    url text NOT NULL,
    created_at timestamptz DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS hash (
    hash varchar(6) NOT NULL PRIMARY KEY
)