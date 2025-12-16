package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.dto.ShortUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.hash.HashCache;
import faang.school.urlshortenerservice.mapper.HashMapper;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repo.UrlCacheRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final UrlMapper urlMapper;
    private final HashMapper hashMapper;

    @Transactional
    public ShortUrlDto makeShortUrl(LongUrlDto longUrlDto) {
        Hash hash = hashCache.getHash();
        Url hashUrlConnection = new Url(hash.toString(), longUrlDto.longUrl());
        urlRepository.save(hashUrlConnection);
        urlCacheRepository.save(hash.toString(),
                longUrlDto.longUrl());

        return hashMapper.shortUrlDto(hash);
    }

    @Transactional
    public LongUrlDto getOriginUrl(String shortUrl) {
        Url longUrl = urlCacheRepository.findLongUrl(shortUrl);
        return urlMapper.toLongUrlDto(longUrl);
    }
}

