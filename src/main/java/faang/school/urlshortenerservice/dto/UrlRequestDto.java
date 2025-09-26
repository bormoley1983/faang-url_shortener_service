package faang.school.urlshortenerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlRequestDto {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String url;
}