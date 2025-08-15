package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;

    @Value("${url-shortener.base-url}")
    private String baseUrl;

    @PostMapping
    public UrlDto createShortUrl(@Valid @RequestBody UrlDto urlDto) {
        log.info("Получен запрос на сокращение URL: {}", urlDto.getOriginalUrl());

        String shortUrl = urlService.createShortUrl(urlDto.getOriginalUrl());

        UrlDto responseDto = new UrlDto();
        responseDto.setOriginalUrl(urlDto.getOriginalUrl());
        responseDto.setShortUrl(shortUrl);

        log.info("Сгенерирована короткая ссылка: {}", shortUrl);
        return responseDto;
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String hash) {
        log.info("Попытка редиректа по хэшу: {}", hash);

        String originalUrl = urlService.getOriginalUrl(hash);

        if (originalUrl == null || originalUrl.isBlank()) {
            log.warn("Ссылка с хэшем '{}' не найдена", hash);
            throw new UrlNotFoundException(hash);
        }

        log.info("Редирект: {} → {}", hash, originalUrl);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(java.net.URI.create(originalUrl))
                .build();
    }
}