package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.service.ShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/urt_shortener")
@RestController
public class UrlController {

    private static final String BASE_GET_REQUEST = "localhost:8079/urt_shortener/";

    private final ShortenerService shortenerService;

    @PostMapping
    public String create(@Valid @RequestBody UrlRequestDto urlRequestDto) {
        String hash = shortenerService.create(urlRequestDto.url());
        return  new StringBuilder(BASE_GET_REQUEST)
                .append(hash)
                .toString();
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> getUrl(@PathVariable String hash) {
        String originalUrl = shortenerService.getUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
