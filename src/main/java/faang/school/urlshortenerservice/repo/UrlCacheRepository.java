package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Hash;
// TODO мохранение в редис
public interface UrlCacheRepository {
    void save(Hash hash);
    Hash find(String key);
}
