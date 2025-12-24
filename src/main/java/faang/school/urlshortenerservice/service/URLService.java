package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.BadRequestException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с хэшами и ассоциациями url - hash.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class URLService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    /**
     * Метод для получения уникального хэша и дальнейшего сохранения ассоциации url - hash в redis и БД
     * @param url - оригинальный url
     * @return строка - уникальный hash
     */
    @Transactional
    public String createHash(String url) {
        validateUrl(url);
        String hash = hashCache.getHash();
        urlRepository.save(new Url(hash, url));
        urlCacheRepository.save(hash, url);
        log.info("hash {} закэширован в Redis и сохранен в БД", hash);
        return hash;
    }

    /**
     * Метод для получения url, ассоциированного с уникальным хэшом. Проверяет наличие в redis, если нет -
     * получает ассоциацию из БД
     * @param hash уникальный хэш, передаваемый пользователем
     * @return оригинальный url для дальнейшего редиректа
     */
    @Transactional(readOnly = true)
    public String getUrl(String hash) {
        String url = urlCacheRepository.getCacheUrl(hash);
        if (url != null) {
            return url;
        }
        url = urlRepository.findUrlByHashOrElseThrow(hash);
        urlCacheRepository.save(hash, url);
        return url;
    }

    /**
     * Проверяет корректность формата url
     * @param url оригинальный url, переданный пользователем
     */
    private void validateUrl(String url) {
        UrlValidator validator = new UrlValidator(new String[]{"http","https","ftp"});
        if (!validator.isValid(url)) {
            log.error("Передан некорректный url: {} ", url);
            throw new BadRequestException("Передан некорректный url");
        }
    }
}
