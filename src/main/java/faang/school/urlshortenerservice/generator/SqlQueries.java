package faang.school.urlshortenerservice.generator;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class SqlQueries {

    public static final String INSERT_HASH =
            "INSERT INTO hash (hash) VALUES (?) ON CONFLICT (hash) DO NOTHING";
}
