CREATE SEQUENCE unique_number_seq
    START WITH 14776336
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE hash (
                      hash VARCHAR(6) PRIMARY KEY
);

CREATE TABLE url (
                     hash VARCHAR(6) PRIMARY KEY,
                     url VARCHAR(256) NOT NULL,
                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                     CONSTRAINT fk_url_hash FOREIGN KEY (hash)
                         REFERENCES hash (hash)
);