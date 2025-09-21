package faang.school.urlshortenerservice.aspect;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    @Pointcut("execution(* faang.school.urlshortenerservice.cache.LocalHashCache.init())")
    public void initLocalHashCachePointcut() {}

    @Pointcut("execution(* faang.school.urlshortenerservice.cache.LocalHashCache.getHash())")
    public void getHashLocalHashCachePointcut() {}

    @Pointcut("execution(* faang.school.urlshortenerservice.repository.UrlCacheRepository.save(..)) "
              + "&& args(url)")
    public void saveUrlCacheRepoPointcut(Url url) {}

    @Pointcut("execution(* faang.school.urlshortenerservice.repository.UrlCacheRepository.getOriginalUrl(..)) "
              + "&& args(hash)")
    public void getOriginalUrlCacheRepoPointcut(String hash) {}

    @Pointcut("execution(* faang.school.urlshortenerservice.service.UrlService.getShortUrl(..)) "
              + "&& args(dto)")
    public void getShortUrlServicePointcut(UrlDto dto) {}

    @Pointcut("execution(* faang.school.urlshortenerservice.service.UrlService.getOriginalUrl(..)) "
              + "&& args(hash)")
    public void getOriginalUrlServicePointcut(String hash) {}

    @Pointcut("execution(* faang.school.urlshortenerservice.scheduler.CleanerScheduler.cleanExpiredUrl())")
    public void cleanExpiredUrlCleanerSchedulerPointcut() {}

    @Pointcut("execution(* faang.school.urlshortenerservice.scheduler.HashScheduler.generateHashes())")
    public void generateHashesHashSchedulerPointcut() {}

    @Pointcut("execution(* faang.school.urlshortenerservice.service.HashGenerator.*(..))")
    public void hashGeneratorPointcut() {}
}
