package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.dto.ShortUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;

import java.util.Optional;

public interface UrlCacheRepository {
    void save(String hash, String longUrl);
    Url findLongUrl(String hash);
}
