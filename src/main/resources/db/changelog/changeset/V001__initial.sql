-- changeset faang-school:1
CREATE TABLE url (
    hash varchar(6) PRIMARY KEY,
    url varchar(255) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT current_timestamp
);

-- changeset faang-school:2
CREATE TABLE hash (
    hash varchar(6) PRIMARY KEY
);

-- changeset faang-school:3
CREATE SEQUENCE unique_number_seq
  START WITH 1
  INCREMENT BY 1;
