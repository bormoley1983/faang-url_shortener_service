package faang.school.urlshortenerservice.exception.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import faang.school.urlshortenerservice.exception.GlobalExceptionHandler;

/**
 * Аннотация для пометки исключений с информацией об HTTP статусе и сообщении.
 * Используется {@link GlobalExceptionHandler} для автоматической обработки.
 *
 * @author bozya
 * @since 11.09.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpStatusError {
    HttpStatus value();
    String message() default "Ошибка";
}