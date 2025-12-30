package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record LongUrlDto(
        @NotBlank
        @URL(message = "Invalid URL")
        String longUrl
) {

}