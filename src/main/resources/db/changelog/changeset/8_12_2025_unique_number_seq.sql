CREATE SEQUENCE unique_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE hash (
                      hash VARCHAR(5) PRIMARY KEY
);

CREATE TABLE url (
                     hash VARCHAR(5) PRIMARY KEY,
                     url TEXT NOT NULL,
                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                     CONSTRAINT fk_url_hash FOREIGN KEY (hash)
                         REFERENCES hash (hash)
);