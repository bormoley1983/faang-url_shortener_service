package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.RedirectResponce;
import faang.school.urlshortenerservice.dto.ShortUrlRequest;
import faang.school.urlshortenerservice.dto.ShortUrlResponce;
import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class UrlFacade {
    @Value("${shortener.address}")
    private String shortenerAddress;
    private final UrlService urlService;

    public RedirectResponce getActualUrl(String hash) {
        String actualUrl = urlService.getActualUrl(hash).getActualUrl();
        return new RedirectResponce(URI.create(actualUrl));
    }

    public ShortUrlResponce createShortUrl(ShortUrlRequest shortUrlRequest) {
        ShortUrl shortUrl = urlService.createShortUrl(shortUrlRequest);
        return createShortUrlResponse(shortUrl);
    }

    private ShortUrlResponce createShortUrlResponse(ShortUrl shortUrl) {
        URI shortUrlValue = URI.create(shortenerAddress.concat(shortUrl.getHash()));

        return new ShortUrlResponce(
                shortUrlValue,
                shortUrl.getExpireTime());
        }
    }
