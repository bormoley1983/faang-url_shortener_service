package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для передачи пользователем оригинальной url ссылки. Не может быть null.
 */
@Data
public class UrlRequest {
    @NotNull
    private String url;
}
