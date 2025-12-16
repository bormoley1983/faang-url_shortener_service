package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ShortUrlDto(
        Long id,
        @NotBlank
        String shortUrl
) {
}
