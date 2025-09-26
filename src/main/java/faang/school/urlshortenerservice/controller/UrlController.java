package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.dto.UrlResponseDto;
import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
@Validated
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<UrlResponseDto> createShortUrl(@Valid @RequestBody UrlRequestDto urlRequestDto) {
        log.info("Received request to create short URL for: {}", urlRequestDto.getUrl());

        UrlResponseDto response = urlService.createShortUrl(urlRequestDto.getUrl());

        log.info("Successfully created short URL: {} for original URL: {}",
                response.getShortUrl(), response.getOriginalUrl());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String hash) {
        log.info("Received request to redirect by hash: {}", hash);

        String originalUrl = urlService.getOriginalUrl(hash);

        log.info("Redirecting hash {} to original URL: {}", hash, originalUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }
}