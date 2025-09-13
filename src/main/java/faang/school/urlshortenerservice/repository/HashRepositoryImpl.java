package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Реализация кастомного репозитория для работы с хэшами.
 * <p>
 * Отвечает за генерацию уникальных чисел, сохранение хэшей батчем
 * и выдачу случайных свободных хэшей для URL Shortener сервиса.
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
@Repository
@RequiredArgsConstructor
public class HashRepositoryImpl implements HashRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Получает n уникальных чисел из sequence unique_number_seq.
     *
     * @param n количество чисел
     * @return список уникальных чисел
     */
    @Override
    public List<Long> getUniqueNumbers(int n) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?)";
        return jdbcTemplate.queryForList(sql, Long.class, n);
    }

    /**
     * Сохраняет список хэшей батчем в таблицу hash.
     *
     * @param hashes список хэшей для сохранения
     */
    @Override
    public void saveBatch(List<String> hashes) {
        String sql = "INSERT INTO hash (hash) VALUES (?)";
        jdbcTemplate.batchUpdate(sql, hashes, hashes.size(),
                (ps, hash) -> ps.setString(1, hash));
    }

    /**
     * Получает n случайных свободных хэшей и удаляет их из таблицы hash.
     *
     * @param n количество хэшей
     * @return список хэшей
     */
    @Override
    public List<String> getHashBatch(int n) {
        String sql = "DELETE FROM hash WHERE hash IN (SELECT hash FROM hash LIMIT ?) RETURNING hash";
        return jdbcTemplate.queryForList(sql, String.class, n);
    }
}