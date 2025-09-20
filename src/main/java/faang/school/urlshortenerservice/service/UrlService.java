package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlViewDto;

/**
 * Сервис для взаимодействия с URL
 *
 * @author Linempy
 * @since 13.09.2025
 */
public interface UrlService {

    /**
     * Метод создает хэш для передаваемого URL.
     * В случае если для URL уже существует хэш, то функция вернет это значение
     *
     * @param createDto исходное DTO с URL
     * @return хэш, который ассоциируется с передаваемым URL
     */
    UrlViewDto createShortUrl(UrlCreateDto createDto);

    /**
     * Метод для получения оригинального URL по хэшу для перенаправления
     *
     * @param hash хэш, ассоциативный с оригинальным URL
     * @return строка с оригинальным URL
     */
    String getOriginUrl(String hash);
}