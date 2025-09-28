package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateUrlDto(
        @NotBlank(message = "Url cannot be empty")
        @Size(max = 2048, message = "Url cannot be longer than 2048 characters")
        @URL(message = "Url is not valid")
        String url
) {
}
