CREATE SEQUENCE unique_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE hash (
                      hash VARCHAR(6) NOT NULL,
                      CONSTRAINT pk_hash PRIMARY KEY (hash),
                      CONSTRAINT ck_hash_length CHECK (LENGTH(hash) <= 6),
                      CONSTRAINT ck_hash_format CHECK (hash ~ '^[0-9a-zA-Z]+$')
    );

CREATE TABLE url (
                     hash VARCHAR(6) NOT NULL,
                     url VARCHAR(2048) NOT NULL,
                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     CONSTRAINT pk_url PRIMARY KEY (hash),
                     CONSTRAINT fk_url_hash FOREIGN KEY (hash) REFERENCES hash(hash) ON DELETE CASCADE,
                     CONSTRAINT ck_url_length CHECK (LENGTH(url) <= 2048),
                     CONSTRAINT ck_url_not_empty CHECK (LENGTH(TRIM(url)) > 0)
);

CREATE INDEX idx_url_created_at ON url(created_at);
CREATE INDEX idx_url_url ON url(url);