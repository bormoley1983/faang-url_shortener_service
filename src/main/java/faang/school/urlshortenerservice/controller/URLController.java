package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.URLRequestDto;
import faang.school.urlshortenerservice.dto.URLResponseDto;
import faang.school.urlshortenerservice.exception.InvalidURLException;
import faang.school.urlshortenerservice.exception.URLNotFoundException;
import faang.school.urlshortenerservice.service.URLService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class URLController {
    private final URLService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenURL(@Valid @RequestBody URLRequestDto request) {
        try {
            String response = urlService.createShortURL(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error shortening URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String hash) {
        try {
            String originalUrl = urlService.getOriginalURL(hash);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, originalUrl)
                    .build();
        } catch (URLNotFoundException e) {
            log.warn("URL not found for hash: {}", hash);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving URL for hash: {}", hash, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Здоровье сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
