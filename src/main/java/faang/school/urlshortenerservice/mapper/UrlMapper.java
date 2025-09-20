package faang.school.urlshortenerservice.mapper;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlViewDto;
import faang.school.urlshortenerservice.entity.UrlEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Маппер для получения DTO для сущности {@link UrlEntity}
 *
 * @author Linempy
 * @since 14.09.2025
 */
@Component
@RequiredArgsConstructor
public class UrlMapper {

    @Value("${app.short-url.base}")
    private String baseUrl;

    public UrlEntity toEntity(UrlCreateDto createDto, String hash) {
        UrlEntity url = new UrlEntity();
        url.setUrl(createDto.longUrl());
        url.setHash(hash);

        return url;
    }

    public UrlViewDto toViewDto(String hash) {
        return new UrlViewDto(buildShortUrl(hash));
    }

    public UrlViewDto toViewDto(UrlEntity url) {
        return new UrlViewDto(buildShortUrl(url.getHash()));
    }

    private String buildShortUrl(String hash) {
        return baseUrl + "/" + hash;
    }
}