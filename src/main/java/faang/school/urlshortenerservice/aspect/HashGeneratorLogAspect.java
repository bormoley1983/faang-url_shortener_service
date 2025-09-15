package faang.school.urlshortenerservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class HashGeneratorLogAspect {
    @Before("Pointcuts.hashGeneratorPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (args != null && args.length > 0) {
            log.info("{} called with args {}", methodName, args);
        } else {
            log.info("{} called", methodName);
        }
    }

    @After("Pointcuts.hashGeneratorPointcut()")
    public void logAfterReturning(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("{} completed", methodName);
    }
}
