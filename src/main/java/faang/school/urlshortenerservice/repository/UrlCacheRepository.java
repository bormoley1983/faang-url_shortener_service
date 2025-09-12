package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;

public interface UrlCacheRepository {

    String findUrlByHash(Hash hash);

    void saveUrl(String hash, String originalUrl);
}
