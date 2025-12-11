package faang.school.urlshortenerservice.service;

/**
 * Сервис для работы с короткими ссылками
 */
public interface HashCache {

    /**
     * Получить один свободный хэш из кеша
     *
     * Если хешей мало, асинхронно загружает дополнительные из БД
     *
     * @return свободный хеш
     */
    String getHash();
}
