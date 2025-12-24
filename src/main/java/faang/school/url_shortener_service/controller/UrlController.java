package faang.school.url_shortener_service.controller;

import faang.school.url_shortener_service.dto.UrlRequestDto;
import faang.school.url_shortener_service.service.ShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@RequestMapping("/url-shortener")
@RestController
public class UrlController {
//    @Value("${base.get.request}")
    private String baseGetRequest = "http://localhost:8080/url-shortener/";

    private final ShortenerService shortenerService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String create(@Valid @RequestBody UrlRequestDto urlRequestDto) {
        String hash = shortenerService.create(urlRequestDto.url());
        return  UriComponentsBuilder
                .fromHttpUrl(baseGetRequest)
                .path(hash)
                .build()
                .toUriString();
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> getUrl(@PathVariable String hash) {
        String originalUrl = shortenerService.getUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
