package faang.school.urlshortenerservice.validation;

import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.exception.InvalidUrlException;
import faang.school.urlshortenerservice.exception.UrlExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@Slf4j
public class UrlValidator {
    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL cannot be null or empty");
        }

        if (url.length() > 2048) {
            throw new InvalidUrlException(url, "URL exceeds maximum length of 2048 characters");
        }

        validateUrlFormat(url);
        validateUrlProtocol(url);
        log.debug("URL validated successfully: {}", url);
    }

    public void validateUrlNotExpired(ShortUrl shortUrl) {
        if(Objects.nonNull(shortUrl.getExpireTime()) &&
                shortUrl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortUrl.getHash(), shortUrl.getExpireTime());
        }
    }

    private void validateUrlFormat(String url) {
        try {
            URI uri = new URI(url);

            if (uri.getScheme() == null || uri.getScheme().isBlank()) {
                throw new InvalidUrlException(url, "URL scheme (http/https) is missing");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException(url, "URL host is missing");
            }

            new URL(url).toURI();

        } catch (URISyntaxException | MalformedURLException e) {
            throw new InvalidUrlException(url, "Invalid URL format: " + e.getMessage());
        }
    }

    private void validateUrlProtocol(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme().toLowerCase();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new InvalidUrlException(url,
                        String.format("Unsupported protocol '%s'. Only HTTP and HTTPS are allowed", scheme));
            }
        } catch (URISyntaxException e) {
            throw new InvalidUrlException(url, "Failed to parse URL for protocol validation");
        }
    }


}
