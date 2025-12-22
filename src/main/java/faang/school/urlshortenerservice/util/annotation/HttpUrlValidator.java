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

        String v = value.trim();

        try {
            URI uri = URI.create(v);

            if (!uri.isAbsolute()) {
                return false;
            }

            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme)
                    && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            if (uri.getUserInfo() != null) {
                return false;
            }

            return !v.contains(" ");

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}