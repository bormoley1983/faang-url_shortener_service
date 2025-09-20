package faang.school.urlshortenerservice.dto;

/**
 * UrlShortDto — неизменяемая структура данных (record).
 * <p>
 * DTO - для возвращения короткой ссылки пользователю
 * </p>*
 *
 * @param shortUrl - готовая ссылка, которая идентична той, которую мы получили от пользователя
 * @author andreyfomchenko
 * @since 17.09.2025
 */
public record UrlShortDto(
        String shortUrl
) {
}