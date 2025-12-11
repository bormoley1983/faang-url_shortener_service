package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private HashCash hashCash;

    @Cacheable(key = "#hash", value = "url")
    public String getUrl(String hash) {
        return urlRepository.findUrlByHash(hash)
                .orElseThrow(() -> new EntityNotFoundException("Url not found"));
    }

    public String shortenUrl(UrlDto dto) {

    }
}