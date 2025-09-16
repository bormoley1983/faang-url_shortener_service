package faang.school.urlshortenerservice.repository;

import java.util.List;

/**
 * Кастомный репозиторий для работы с хэшами.
 * <p>
 * Определяет методы для генерации и выдачи уникальных хэшей,
 * которые не покрываются стандартными CRUD операциями JPA.
 * </p>
 * <ul>
 *     <li>{@link #getUniqueNumbers(int)} — получает n уникальных чисел из sequence для генерации Base62-хэшей.</li>
 *     <li>{@link #saveBatch(List)} — сохраняет список хэшей в таблицу hash батчем.</li>
 *     <li>{@link #getHashBatch(int)} — получает n случайных свободных хэшей и удаляет их из таблицы.</li>
 * </ul>
 *
 * @author agent
 * @since 12.09.2025
 */
public interface HashRepositoryCustom {

    List<Long> getUniqueNumbers(int n);

    void saveBatch(List<String> hashes);

    List<String> getHashBatch(int n);
}