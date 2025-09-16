package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.HashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с короткими хэшами.
 * <p>
 * Отвечает за хранение, выдачу и генерацию уникальных хэшей для URL Shortener сервиса.
 * Расширяет стандартные CRUD методы JPA и кастомные методы {@link HashRepositoryCustom}.
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
public interface HashRepository extends JpaRepository<HashEntity, String>, HashRepositoryCustom {
}