package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.dto.UrlShortDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/short")
public class UrlController {

    private final UrlService urlService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    public ResponseEntity<UrlShortDto> createShortUrl(@RequestBody @Valid UrlRequestDto requestDto) {
        String hash = urlService.createShortUrl(requestDto.url());
        String shortUrl = baseUrl.endsWith("/")
                ? baseUrl + "short/" + hash
                : baseUrl + "/short/" + hash;
        return ResponseEntity.ok(new UrlShortDto(shortUrl));
    }

    @GetMapping("/{hash}")
    public RedirectView redirectToUrl(@PathVariable String hash) {
        String url = urlService.findOriginalUrl(hash);
        return new RedirectView(url);
    }
}
