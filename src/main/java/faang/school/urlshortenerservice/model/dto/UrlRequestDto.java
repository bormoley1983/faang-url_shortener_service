package faang.school.urlshortenerservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса на создание короткой ссылки
 */
public record UrlRequestDto(
        @NotBlank(message = "URL не должен быть пустым")
        @Size(max = 2048, message = "URL слишком длинный")
        @Pattern(
                regexp = "^(http|https)://.+$",
                message = "URL должен начинаться с http:// или https://"
        )
        String url
) {
}