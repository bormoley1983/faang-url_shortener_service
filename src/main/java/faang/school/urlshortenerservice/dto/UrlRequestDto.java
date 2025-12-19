package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record UrlRequestDto(
        @NotBlank(message = "Url сannot be empty")
        @URL(message = "Invalid URL format")
        @Pattern(
                regexp = URL_PATTERN,
                message = "Only HTTP/HTTPS protocols and .com, .org, .net, .ru domains allowed"
        )
        String url
) {
public static final String URL_PATTERN = "^(https?://)[\\w.-]+\\.(com|org|net|ru)(:[0-9]+)?(/.*)?$";
}
