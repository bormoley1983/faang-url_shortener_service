package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Hash;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HashRepositoryJdbc {

    private final JdbcTemplate jdbcTemplate;

    public List<Long> getUniqueNumbers(int count) {
        String sql = """
                SELECT nextval('unique_number_seq')
                FROM generate_series(1, ?)
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong(1),
                count
        );
    }

    public List<Hash> getHashBatch(int amount) {
        String sql = """
                DELETE FROM hash
                WHERE hash IN (
                    SELECT hash
                    FROM hash
                    LIMIT ?
                )
                RETURNING hash
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Hash hash = new Hash();
                    hash.setHash(rs.getString("hash"));
                    return hash;
                },
                amount
        );
    }

    public Integer saveAll(List<Hash> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder(
                "INSERT INTO hash (hash) VALUES "
        );

        List<Object> params = new ArrayList<>();

        for (int i = 0; i < hashes.size(); i++) {
            Hash hash = hashes.get(i);
            sql.append("(?)");

            if (i < hashes.size() - 1) {
                sql.append(", ");
            }

            params.add(hash.getHash());

        }

        sql.append(" ON CONFLICT (hash) DO NOTHING");

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

}