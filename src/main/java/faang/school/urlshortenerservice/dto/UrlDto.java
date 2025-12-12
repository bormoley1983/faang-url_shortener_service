package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UrlDto(

        @NotNull(message = "URL cannot be null")
        @NotBlank(message = "URL cannot be blank")
        @Size(max = 2048, message = "URL so long")
        @Pattern(
                regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
                message = "Unknown URL format"
        )
        String url
) {
}