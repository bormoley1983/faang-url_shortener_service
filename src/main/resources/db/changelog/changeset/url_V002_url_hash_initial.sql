CREATE TABLE hash (
    hash VARCHAR(7) PRIMARY KEY
);

CREATE TABLE url_hash (
    hash      varchar(7) UNIQUE NOT NULL PRIMARY KEY,
    url       varchar(256) NOT NULL,
    create_at timestamptz DEFAULT current_timestamp
);

