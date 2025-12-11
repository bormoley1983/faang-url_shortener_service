package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.CreateUrlDto;

/**
 * Сервис для работы с короткими ссылками
 */
public interface UrlService {

    /**
     * Получить оригинальный URL по короткому хешу
     *
     * @param hash короткий хеш из URL
     * @return оригинальный длинный URL
     * @throws UrlNotFoundException если URL не найден
     */
    String getOriginalUrl(String hash);

    /**
     * Создать короткую ссылку для переданного URL
     *
     * @param createUrlDto DTO с исходным URL
     * @return короткую ссылку
     */
    String createShortUrl(CreateUrlDto createUrlDto);
}