package faang.school.urlshortenerservice.repository;

import java.util.List;

/**
 * Репозиторий для работы с хэшами.
 * <p>
 * Определяет методы для получение, генерации и выдачи уникальных хэшей,
 * </p>
 * <ul>
 *     <li>{@link #getUniqueNumbers(int)} — получает n уникальных чисел из sequence для генерации Base62-хэшей.</li>
 *     <li>{@link #save(List)} — сохраняет список хэшей в таблицу hash батчем.</li>
 *     <li>{@link #getHashBatch(int)} — получает n случайных свободных хэшей и удаляет их из таблицы.</li>
 * </ul>
 *
 * @author andreyFomchenko
 * @since 17.09.2025
 */
public interface HashRepositoryUtil {

    List<Long> getUniqueNumbers(int n);

    void save(List<String> hashes);

    List<String> getHashBatch(int n);
}
