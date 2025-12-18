package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.dto.ShortUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlShortenerException;
import faang.school.urlshortenerservice.hash.HashCache;
import faang.school.urlshortenerservice.mapper.HashMapper;
import faang.school.urlshortenerservice.repo.UrlCacheRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final HashMapper hashMapper;

    @Transactional
    public ShortUrlDto makeShortUrl(LongUrlDto longUrlDto) {
        Hash hash = hashCache.getHash();
        Url url = new Url(hash.getHash(), longUrlDto.longUrl());

        urlRepository.save(url);

        urlCacheRepository.save(hash.getHash(), longUrlDto.longUrl());

        return hashMapper.shortUrlDto(hash);
    }

    @Transactional
    public LongUrlDto getOriginUrl(String shortUrl) {
        Optional<String> cached = urlCacheRepository.findLongUrl(shortUrl);
        String longUrl = cached.orElseGet(() -> {
            Url url = urlRepository.shortUrl(shortUrl)
                    .orElseThrow(() -> new UrlShortenerException("URL not found"));
            urlCacheRepository.save(shortUrl, url.getLongUrl());
            return url.getLongUrl();
        });

        return new LongUrlDto(longUrl);
    }
}

