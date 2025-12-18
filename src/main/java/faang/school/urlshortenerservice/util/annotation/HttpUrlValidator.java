package faang.school.urlshortenerservice.util.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            var uri = URI.create(value.trim());

            if (!uri.isAbsolute()) {
                return false;
            }

            var scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }

            var host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            if (uri.getUserInfo() != null) {
                return false;
            }
            return !value.contains(" ");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}