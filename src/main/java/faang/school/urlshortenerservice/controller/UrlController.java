package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.RedirectResponce;
import faang.school.urlshortenerservice.dto.ShortUrlRequest;
import faang.school.urlshortenerservice.dto.ShortUrlResponce;
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

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UrlController {
    private final UrlFacade urlFacade;

    @GetMapping("/{hash}")
    public ResponseEntity<RedirectResponce> getActualUrl(@PathVariable String hash) {
        RedirectResponce redirectResponce = urlFacade.getActualUrl(hash);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectResponce.url())
                .build();
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponce> createShortUrl (@Valid @RequestBody ShortUrlRequest shortUrlRequest) {
        ShortUrlResponce shortUrlResponce = urlFacade.createShortUrl(shortUrlRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shortUrlResponce);
    }
}
