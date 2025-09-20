package faang.school.urlshortenerservice.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Компонент для валидации URL
 *
 * @author Linempy
 * @since 13.09.2025
 */
@Component
public class UrlValidator {

    public static final String URL_REGEX = "^(https?):\\/\\/([^\\s\\/$.?#].[^\\s]*)$";

    public static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?):\\/\\/([^\\s\\/$.?#].[^\\s]*)$",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isInvalidUrl(String url) {
        return !URL_PATTERN.matcher(url).matches();
    }

}