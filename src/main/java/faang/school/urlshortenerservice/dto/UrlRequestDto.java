package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * UrlRequestDto — описание класса.
 * <p>
 * DTO для создания запроса на получение короткого URL
 * </p>*
 *
 * @author andreyfomchenko
 * @since 17.09.2025
 */
public record UrlRequestDto(
        @NotBlank(message = "URL не должен быть пустым")
        @Size(max = 256, message = "URL слишком длинный")
        @Pattern(
                regexp = "^(http|https)://.+$",
                message = "URL должен начинаться с http:// или https://"
        )
        String url
) {
}
