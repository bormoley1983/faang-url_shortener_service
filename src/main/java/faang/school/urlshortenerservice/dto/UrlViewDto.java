package faang.school.urlshortenerservice.dto;

import faang.school.urlshortenerservice.entity.UrlEntity;

/**
 * DTO для сущности {@link UrlEntity}
 *
 * @author Linempy
 * @since 14.09.2025
 */
public record UrlViewDto(
        String shortUrl
){
}