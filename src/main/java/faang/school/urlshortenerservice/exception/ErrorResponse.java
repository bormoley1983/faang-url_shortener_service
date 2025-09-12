package faang.school.urlshortenerservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Временная метка возникновения ошибки
     */
    private LocalDateTime timestamp;

    /**
     * HTTP статус код
     */
    private int status;

    /**
     * Краткое описание типа ошибки
     */
    private String error;

    /**
     * Подробное сообщение об ошибке
     */
    private String message;

    /**
     * Путь к эндпоинту где произошла ошибка
     */
    private String path;

    /**
     * Детали ошибок валидации (только для ошибок валидации)
     */
    private Map<String, String> validationErrors;
}