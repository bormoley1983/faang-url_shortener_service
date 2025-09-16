package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.model.dto.UrlRequestDto;
import faang.school.urlshortenerservice.model.dto.UrlResponseDto;
import faang.school.urlshortenerservice.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/short")
public class UrlController {

    private final UrlService urlService;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * POST /url — создание короткой ссылки
     */
    @PostMapping
    public ResponseEntity<UrlResponseDto> createShortUrl(@RequestBody @Valid UrlRequestDto requestDto) {
        String hash = urlService.createShortUrl(requestDto.url());
        String shortUrl = baseUrl.endsWith("/")
                ? baseUrl + "short/" + hash
                : baseUrl + "/short/" + hash;
        return ResponseEntity.ok(new UrlResponseDto(shortUrl));
    }

    /**
     * GET /short/{hash} — редирект на длинную ссылку
     */
    @GetMapping("/{hash}")
    public RedirectView redirectToLongUrl(@PathVariable String hash) {
        String longUrl = urlService.getLongUrl(hash);
        return new RedirectView(longUrl);
    }
}