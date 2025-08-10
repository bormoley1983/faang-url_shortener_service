--liquibase formatted sql

--changeset petr.martyshov:1.0-initial-schema splitStatements:false stripComments:false runOnChange:false

-- Создание таблицы hashes
CREATE TABLE ${db.schema}.hashes
(
    hash VARCHAR(6) NOT NULL, -- хэш короткой ссылки
    CONSTRAINT hashes_pk PRIMARY KEY (hash)
);

-- Создание таблицы urls
CREATE TABLE ${db.schema}.urls
(
    hash       VARCHAR(6) NOT NULL, -- хэш короткой ссылки
    url        VARCHAR    NOT NULL, -- оригинальная ссылка
    created_at TIMESTAMP  NOT NULL, -- дата и время создания короткой ссылки
    CONSTRAINT urls_pk PRIMARY KEY (hash)
);

-- Создание последовательности unique_number_seq
CREATE SEQUENCE ${db.schema}.unique_number_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

-- Добавление комментариев
COMMENT ON COLUMN ${db.schema}.hashes.hash IS 'хэш короткой ссылки';

COMMENT ON COLUMN ${db.schema}.urls.hash IS 'хэш короткой ссылки';
COMMENT ON COLUMN ${db.schema}.urls.url IS 'оригинальная ссылка';
COMMENT ON COLUMN ${db.schema}.urls.created_at IS 'дата и время создания короткой ссылки';

ALTER TABLE ${db.schema}.hashes OWNER TO "${db.owner}";
GRANT ALL ON TABLE ${db.schema}.hashes TO "${db.owner}";

ALTER TABLE ${db.schema}.urls OWNER TO "${db.owner}";
GRANT ALL ON TABLE ${db.schema}.urls TO "${db.owner}";

ALTER SEQUENCE ${db.schema}.unique_number_seq OWNER TO "${db.owner}";
GRANT ALL ON SEQUENCE ${db.schema}.unique_number_seq TO "${db.owner}";
