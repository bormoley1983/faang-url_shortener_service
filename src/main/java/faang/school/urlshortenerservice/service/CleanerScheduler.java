package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Планировщик для очистки устаревших URL и возврата их хэшей в пул.
 *
 * <p>
 * Класс {@code CleanerScheduler} периодически проверяет таблицу URL в базе данных
 * и удаляет записи, созданные более года назад. После удаления хэши освобожденных
 * URL сохраняются обратно в таблицу хэшей для повторного использования.
 * </p>
 *
 * <p>
 * Поведение:
 * <ul>
 *     <li>Определяет "устаревшие" URL, старше одного года от текущего времени.</li>
 *     <li>Удаляет все такие записи из таблицы URL.</li>
 *     <li>Передает их хэши в {@link HashRepository} для сохранения в пул доступных хэшей.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Метод {@link #cleanOldUrls()} запускается автоматически в соответствии с cron-выражением
 * {@code ${cleaner.cron.daily}} и помечен как {@link Transactional}, чтобы все операции
 * удаления и сохранения хэшей выполнялись атомарно.
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
@Component
@RequiredArgsConstructor
@EnableScheduling
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Scheduled(cron = "${cleaner.cron.daily}")
    @Transactional
    public void cleanOldUrls() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(1);
        List<String> oldHashes = urlRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isBefore(cutoff))
                .map(u -> u.getHash())
                .toList();

        if (!oldHashes.isEmpty()) {
            urlRepository.deleteAllById(oldHashes);
            hashRepository.saveBatch(oldHashes);
        }
    }
}