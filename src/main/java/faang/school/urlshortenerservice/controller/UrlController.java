package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.UrlService;
import faang.school.urlshortenerservice.validator.PayloadValidator;
import jakarta.validation.constraints.NotBlank;
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
import java.net.URI;

@RestController
@RequestMapping("/v1/urls")
@Validated
@RequiredArgsConstructor
public class UrlController {
    private final UrlService service;
    private final PayloadValidator validator;

    @PostMapping("/short")
    public UrlDto shorten(@RequestBody UrlDto urlDto) {
        validator.validateUrl(urlDto.url());
        return service.getShortUrl(urlDto);
    }

    @GetMapping("/redirect/{hash}")
    public ResponseEntity<Void> redirect(@PathVariable @NotBlank String hash) {
        String url = service.getOriginalUrl(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
