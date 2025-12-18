package faang.school.urlshortenerservice.service;

public interface UrlService {
    /**
     * Создать короткую ссылку для переданного URL
     *
     * @param longUrl String с исходным URL
     * @return String короткую ссылку
     */
    String createShortUrl(String longUrl);
}
