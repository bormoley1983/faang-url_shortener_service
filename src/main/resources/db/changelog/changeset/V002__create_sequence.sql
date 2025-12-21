-- Последовательность для генерации уникальных ID
-- используется для генерации хэшей через base62
CREATE SEQUENCE IF NOT EXISTS url_sequence
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    MAXVALUE 9223372036854775807
    CACHE 1000;

ALTER SEQUENCE hashes_id_seq INCREMENT BY 1000;