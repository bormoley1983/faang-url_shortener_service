package faang.school.urlshortenerservice.exception;

/**
 * Стандартизированный ответ об ошибке для REST API.
 * <p>
 * Используется для возврата структурированной информации об ошибках клиенту
 * в едином формате. Содержит сообщение об ошибке.
 * </p>
 *
 * @author bozya
 * @since 10.09.2025
 */
public record ErrorResponse(String message) {
}