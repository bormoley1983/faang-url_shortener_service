package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequest;
import faang.school.urlshortenerservice.service.URLService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы со ссылками. Имеет методы для получения сокращенной ссылки на основе
 * уникально сгенерированного хэша и редиректа пользователя на оригинальную ссылку
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/short-url")
public class URLController {

    private final URLService service;
    @Value("${shortener.host}")
    private String host;

    @PostMapping
    public ResponseEntity<String> createHash(@Valid @RequestBody UrlRequest request) {
        var hash = service.createHash(request.getUrl());
        return ResponseEntity.ok(host + hash);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> getUrl(@PathVariable String hash) {
        String url = service.getUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}
