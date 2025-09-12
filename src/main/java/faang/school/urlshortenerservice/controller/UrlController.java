package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.CreateUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.exception.DataValidationException;
import faang.school.urlshortenerservice.exception.InternalServerError;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/url")
    public ResponseEntity<String> createShortUrl(@RequestBody @Valid CreateUrlDto createUrlDto) {
        try {
            String originalUrl = createUrlDto.originalUrl();

            if (originalUrl == null
                    || originalUrl.isEmpty()
                    || originalUrl.length() > 2048) {
                throw new DataValidationException("Invalid URL");
            }

            String hash = urlService.createShortUrl(originalUrl);
            return ResponseEntity.ok(hash);
        } catch (InternalServerError e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при создании короткой ссылки: " + e.getMessage());
        }
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirect(@PathVariable @Valid Hash hash) {
        log.info("Получен запрос на редирект для хеша: {}", hash.getHashValue());
        String originalUrl = urlService.getUrl(hash);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}