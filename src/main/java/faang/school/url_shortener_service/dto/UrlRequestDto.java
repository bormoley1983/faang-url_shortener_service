package faang.school.url_shortener_service.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UrlRequestDto(
    @NotBlank
    @URL
    String url
) {
}