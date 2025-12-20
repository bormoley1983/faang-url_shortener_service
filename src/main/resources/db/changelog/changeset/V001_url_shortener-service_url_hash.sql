-- Таблица для хранения ассоциаций: короткий хэш -> длинный URL
CREATE TABLE url (
hash        VARCHAR (6) PRIMARY KEY,
url         TEXT NOT NULL,
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для хранения всех сгенерированных хэшей
CREATE TABLE hash (
hash        VARCHAR(6) PRIMARY KEY
);

-- Последовательность для генерации уникальных чисел
CREATE SEQUENCE unique_number_seq START WITH 1;