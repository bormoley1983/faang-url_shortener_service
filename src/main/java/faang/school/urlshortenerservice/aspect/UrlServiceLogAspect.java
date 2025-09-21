package faang.school.urlshortenerservice.aspect;

import faang.school.urlshortenerservice.dto.UrlDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UrlServiceLogAspect {
    @Before("Pointcuts.getShortUrlServicePointcut(dto)")
    public void logBeforeGetShortUrl(UrlDto dto) {
        log.info("Creating a short link for url = {}", dto.url());
    }

    @AfterReturning(value = "Pointcuts.getShortUrlServicePointcut(dto)", returning = "result")
    public void logAfterReturningGetShortUrl(UrlDto dto, UrlDto result) {
        log.info("Short link {} created with hash = {} for url = {}", result.url(), result.hash(), dto.url());
    }

    @Before("Pointcuts.getOriginalUrlServicePointcut(hash)")
    public void logBeforeGetOriginalUrl(String hash) {
        log.info("Getting original url by hash = {}", hash);
    }

    @AfterReturning(value = "Pointcuts.getOriginalUrlServicePointcut(hash)", returning = "result")
    public void logAfterReturningGetOriginalUrl(String hash, String result) {
        log.info("Original url has gotten by hash = {}. Url = {}", hash, result);
    }
}
