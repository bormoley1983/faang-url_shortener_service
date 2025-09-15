package faang.school.urlshortenerservice.validator;

import faang.school.urlshortenerservice.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayloadValidator {
    private final UrlValidator urlValidator;

    public void validateUrl(String url) {
        if (!urlValidator.isValid(url)) {
            throw new ValidationException("Invalid URL {}", url);
        }
    }
}
