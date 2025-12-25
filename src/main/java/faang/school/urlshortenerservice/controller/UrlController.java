package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.dto.ShortUrlDto;
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
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/short")
    public ShortUrlDto getShortUrl(@Valid @RequestBody LongUrlDto longUrlDto) {
        return urlService.makeShortUrl(longUrlDto);
    }

    @GetMapping("/origin/{hash}")
    public LongUrlDto getOriginUrl(@PathVariable String hash) {
        return urlService.getOriginUrl(hash);
    }
}
