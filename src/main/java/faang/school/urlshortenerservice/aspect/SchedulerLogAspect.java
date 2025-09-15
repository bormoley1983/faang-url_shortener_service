package faang.school.urlshortenerservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SchedulerLogAspect {
    @Around("Pointcuts.cleanExpiredUrlCleanerSchedulerPointcut()")
    public void logAroundCleanExpiredUrl(ProceedingJoinPoint proceeding) {
        log.info("Scheduled cleaning expired urls started");
        try {
            proceeding.proceed();
            log.info("Scheduled cleaning expired urls completed");
        } catch (Throwable e) {
            log.error("Scheduled cleaning expired urls is failed. Cause: ", e);
        }
    }

    @Before("Pointcuts.generateHashesHashSchedulerPointcut()")
    public void logBeforeGenerateHashes() {
        log.info("Scheduled hashes generation started");
    }

    @After("Pointcuts.generateHashesHashSchedulerPointcut()")
    public void logAfterGenerateHashes() {
        log.info("Scheduled hashes generation completed");
    }
}
