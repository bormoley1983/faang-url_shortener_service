INSERT INTO urls
VALUES
    ('999999', 'https://www.baeldung.com'),
    ('ZZZZZZ', 'https://www.google.com');

UPDATE urls
    SET last_requested_at = last_requested_at - INTERVAL '1 day'
WHERE hash = '999999';

UPDATE urls
    SET last_requested_at = last_requested_at - INTERVAL '1 year'
WHERE hash = 'ZZZZZZ';