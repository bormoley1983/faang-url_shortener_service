CREATE TABLE url (
  hash varchar(32) PRIMARY KEY,
  long_link varchar(128) NOT NULL,
  created_at timestamptz DEFAULT current_timestamp
);