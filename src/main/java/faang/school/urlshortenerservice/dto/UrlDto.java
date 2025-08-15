package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UrlDto {
    @NotNull(message = "URL не должен быть null")
    @NotBlank(message = "URL не должен быть пустым")
    @URL(message = "Некорректный формат URL")
    private String originalUrl;
    private String shortUrl;
}