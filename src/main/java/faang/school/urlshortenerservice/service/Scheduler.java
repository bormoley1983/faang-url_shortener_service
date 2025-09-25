package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.repository.HashRepositoryUtil;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Компонент-планировщик для выполнения периодических задач по очистке устаревших данных.
 * Использует Spring Scheduling для запуска задач по расписанию.
 *
 * <p>Основная задача - очистка URL-адресов, которые были созданы более года назад,
 * с сохранением их хэшей для предотвращения повторного использования.</p>
 *
 * <p>Расписание выполнения задачи настраивается через свойство {@code scheduler.cron.midnight-everyday}
 * в конфигурационном файле application.yml.</p>
 *
 * @author andreyfomchenko
 * @since 18.09.2025
 */

@Component
@RequiredArgsConstructor
@EnableScheduling
public class Scheduler {

    private final UrlRepository urlRepository;
    private final HashRepositoryUtil hashRepository;

    @Scheduled(cron = "${scheduler.cron.midnight-everyday}")
    @Transactional
    public void cleanerOldUrl() {
        LocalDateTime yearsBefore= LocalDateTime.now().minusYears(1);
        List<String> oldHash = urlRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isBefore(yearsBefore))
                .map(UrlEntity::getHash)
                .toList();
        if (!oldHash.isEmpty()) {
            urlRepository.deleteAllById(oldHash);
            hashRepository.save(oldHash);
        }
    }
}
