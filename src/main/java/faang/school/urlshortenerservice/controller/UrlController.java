package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.CreateUrlRequestDto;
import faang.school.urlshortenerservice.dto.CreateUrlResponseDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @PostMapping
    public CreateUrlResponseDto createUrl(@Valid @RequestBody CreateUrlRequestDto requestBody) {
        String shortUrl = urlService.createShortUrl(requestBody.url());
        return new CreateUrlResponseDto(shortUrl);
    }
}
