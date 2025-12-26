package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception_handler.UrlNotFoundException;
import faang.school.urlshortenerservice.generator.HashCache;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    public String createShortUrl(String longUrl) {
        String hash = hashCache.getHash();
        // Сохраняем в БД
        urlRepository.save(new Url(hash, longUrl));
        // Сохраняем в Redis
        urlCacheRepository.save(hash, longUrl);
        // Формируем короткую ссылку (можно иначе)
        return "http://your-domain/" + hash;
    }

    public String findLongUrlByHash(String hash) {
        // 1. Попытка найти в кэше Redis
        String url = urlCacheRepository.findByHash(hash);
        if (url != null) return url;

        // 2. Если не найдено, ищем в БД
        Optional<Url> urlEntity = urlRepository.findById(hash);
        if (urlEntity.isPresent()) return urlEntity.get().getUrl();

        // 3. Если нигде не найдено — ошибка
        throw new UrlNotFoundException(hash);
    }
}
