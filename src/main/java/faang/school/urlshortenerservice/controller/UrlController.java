package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlViewDto;
import faang.school.urlshortenerservice.service.UrlService;
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

/**
 * REST-контроллер для получения короткой ссылки и перенаправления по ней.
 *
 * @author Linempy
 * @since 13.09.2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/urls")
public class UrlController {

    private final UrlService service;

    @PostMapping
    public ResponseEntity<UrlViewDto> createShortUrl(@Valid @RequestBody UrlCreateDto crateDto) {
        UrlViewDto createdHash = service.createShortUrl(crateDto);
        return ResponseEntity.ok(createdHash);
    }

        @GetMapping("/{hash}")
        public ResponseEntity<Void> getOriginUrl(@PathVariable String hash) {
            String originUrl = service.getOriginUrl(hash);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", originUrl)
                    .build();
        }

}