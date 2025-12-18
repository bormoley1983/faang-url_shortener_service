package faang.school.urlshortenerservice.dto;

import faang.school.urlshortenerservice.util.annotation.HttpUrl;
import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequestDto(
        @NotBlank
        @HttpUrl
        String url
) {}
