package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/url")
public class UrlController {

    private final UrlService urlService;

    @Value("${url-shortener.base-url}")
    private String baseUrl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public URI createShortUrl(@Valid @RequestBody UrlRequestDto request) {
        String hash = urlService.createShortUrl(request.url());

        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment(hash)
                .build()
                .toUri();
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> getShortUrl(@PathVariable String hash) {
        String url = urlService.getShortUrl(hash);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
