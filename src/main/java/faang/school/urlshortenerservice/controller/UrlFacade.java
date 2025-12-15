package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.RedirectResponce;
import faang.school.urlshortenerservice.dto.ShortUrlRequest;
import faang.school.urlshortenerservice.dto.ShortUrlResponce;
import faang.school.urlshortenerservice.entity.ShortUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@RequiredArgsConstructor
public class UrlFacade {
    @Value("${shortener.address}")
    private final String shortenerAddress;
    private final UrlService urlService;

    public RedirectResponce getUrl(String hash) {
        String url = urlService.getUrl(hash).getActualUrl();
        return new RedirectResponce(URI.create(url));
    }

    public ShortUrlResponce createShortUrl(ShortUrlRequest shortUrlRequest) {
        ShortUrl shortUrl = urlService.createShortUrl(shortUrlRequest);
        return createShortUrlResponse(shortUrl);
    }

    private ShortUrlResponce createShortUrlResponse(ShortUrl shortUrl) {
        String fullUrl = shortenerAddress + shortUrl.getHash();
        try {
            URI shortUrlValue = new URI(fullUrl);
            return new ShortUrlResponce(shortUrlValue, shortUrl.getExpire_time());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URL constructed: " + fullUrl, e);
        }
    }
}
