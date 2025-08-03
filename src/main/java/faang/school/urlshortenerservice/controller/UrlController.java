package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
@Validated
public class UrlController {
    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<String> getShortUrl(@Valid @RequestBody UrlRequestDto request,
                                              HttpServletRequest httpRequest) {
        String baseUrl = httpRequest.getRequestURL().toString();
        String shortUrl = urlService.getShortUrl(request.getUrl(), baseUrl);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> getLongUrl(@PathVariable
                                               @Size(max = 6, message = "Hash must be 6 characters or less")
                                               String hash) {
        String longUrl = urlService.getLongUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl.trim().replace("\0", "")))
                .build();
    }
}