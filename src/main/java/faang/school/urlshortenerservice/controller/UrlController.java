package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.service.ShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/urt_shortener")
@RestController
public class UrlController {

    private final ShortenerService shortenerService;

    @PostMapping
    public String create(@Valid @RequestBody UrlRequestDto urlRequestDto) {
        return shortenerService.create(urlRequestDto.url());
    }

    @GetMapping("/{hash}")
    public String getUrl(@PathVariable String hash) {
        return shortenerService.getUrl(hash);
    }
}
