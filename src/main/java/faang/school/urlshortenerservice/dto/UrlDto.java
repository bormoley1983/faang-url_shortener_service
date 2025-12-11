package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UrlDto(

        @NotNull(message = "URL не может быть пустым")
        @NotBlank(message = "URL не может быть пустым")
        @Size(max = 2048, message = "URL слишком длинный")
        @Pattern(
                regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
                message = "Некорректный URL"
        )
        String url
) {
}
