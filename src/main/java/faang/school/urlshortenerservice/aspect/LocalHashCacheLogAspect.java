package faang.school.urlshortenerservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LocalHashCacheLogAspect {
    @Before("Pointcuts.initLocalHashCachePointcut()")
    public void logBeforeGetHash() {
        log.info("Start initializing LocalCache");
    }

    @After("Pointcuts.initLocalHashCachePointcut()")
    public void logAfterGetHash() {
        log.info("LocalCache initialisation complete");
    }
}
