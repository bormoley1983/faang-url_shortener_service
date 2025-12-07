package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUrlDto(
        @NotBlank
        String url
) {
}