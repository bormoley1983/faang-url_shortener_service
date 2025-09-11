package faang.school.urlshortenerservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Стандартизированный ответ об ошибке для REST API.
 * <p>
 * Используется для возврата структурированной информации об ошибках клиенту
 * в едином формате. Содержит сообщение об ошибке и HTTP статус код.
 * </p>
 *
 * @author bozya
 * @since 10.09.2025
 */
public record ErrorResponse(String message, HttpStatus status) {
}