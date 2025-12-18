package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.localcache.LocalCache;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.analytic.AnalyticService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    @Value("${url.ttl-days:30}")
    private long ttlDays;

    private final LocalCache localCache;
    private final UrlMapper urlMapper;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final AnalyticService analyticsService;

    @Override
    @Transactional
    public String createShortUrl(UrlRequestDto dto) {
        Url url = urlMapper.toModel(dto);
        String hash = localCache.getHash();

        url.setHash(hash);
        url.setExpiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS));
        urlRepository.save(url);
        urlCacheRepository.save(hash, url, ttlDays);

        return hash;
    }

    @Override
    public Url getOriginalUrl(String hash, HttpServletRequest request) {
        Url url = urlCacheRepository.get(hash);

        if (url == null) {
            url = urlRepository.findByHash(hash)
                    .orElseThrow(() -> new EntityNotFoundException("URL not found"));
            urlCacheRepository.save(hash, url, ttlDays);
        }

        analyticsService.recordClickAsync(url, request);
        return url;
    }
}
