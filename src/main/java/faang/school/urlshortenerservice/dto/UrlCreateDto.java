package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static faang.school.urlshortenerservice.util.UrlValidator.URL_REGEX;

/**
 * DTO для создания из длинного URL короткий (захэшированный)
 *
 * @author Linempy
 * @since 13.09.2025
 */
public record UrlCreateDto(
        @NotBlank
        @Pattern(regexp = URL_REGEX,
                message = "Невалидный формат URL")
        String longUrl
) {
}