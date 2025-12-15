package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.url.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/url")
@RestController
public class UrlController {
    private final UrlService urlService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UrlDto createUrl(@RequestBody @Valid UrlCreateDto urlCreateDto) {
        return urlService.createUrl(urlCreateDto.userUrl());
    }

    @GetMapping("/{hash}")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<Void> getUrl(@PathVariable String hash) {
        String originalUrl = urlService.getUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}