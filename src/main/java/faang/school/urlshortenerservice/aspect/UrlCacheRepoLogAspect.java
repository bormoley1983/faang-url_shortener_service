package faang.school.urlshortenerservice.aspect;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.EntityAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UrlCacheRepoLogAspect {

    @After("Pointcuts.saveUrlCacheRepoPointcut(url)")
    public void logBeforeSave(Url url) {
        log.info("Original url saved to cache and DB. Hash = {}, url = {}", url.getHash(), url.getUrl());
    }

    @AfterThrowing(pointcut = "Pointcuts.saveUrlCacheRepoPointcut(url)",
                   throwing = "e")
    public void logAfterThrowingSave(Url url, Throwable e) {
        if (e instanceof DataIntegrityViolationException) {
            String message = "A short link already created for url = {}";
            log.error(message, url.getUrl());
            throw new EntityAlreadyExistsException(message, url.getUrl());
        }

        log.error(e.getMessage(), e);
    }

    @Before("Pointcuts.getOriginalUrlCacheRepoPointcut(hash)")
    public void logBeforeGetOriginalUrl(String hash) {
        log.info("Searching original url in cache or DB by hash = {}", hash);
    }
}
