package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/url")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public UrlRequestDto createShortUrl(@Valid @RequestBody UrlRequestDto request) {
        String shortUrl = urlService.createShortUrl(request.url());
        return new UrlRequestDto(shortUrl);
    }

    @GetMapping("/{hash}")
    public String getShortUrl(@PathVariable String url) {
        return urlService.getShortUrl(url);
    }
}
