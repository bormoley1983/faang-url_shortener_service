package faang.school.urlshortenerservice.service;

public interface UrlService {
    /**
     * Создать короткую ссылку для переданного URL
     *
     * @param longUrl String с исходным URL
     * @return String короткую ссылку
     */
    String createShortUrl(String longUrl);

    /**
     * Вернуть длинную ссылку для переданного hash
     *
     * @param hash String с хешом длинной ссылки
     * @return String длинную ссылку
     */
    String getOriginalUrl(String hash);
}
