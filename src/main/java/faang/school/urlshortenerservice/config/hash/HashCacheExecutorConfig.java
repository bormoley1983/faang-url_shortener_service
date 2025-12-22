package faang.school.urlshortenerservice.config.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class HashCacheExecutorConfig {

    private static final String THREAD_NAME_PREFIX = "hash-cache-executor-";

    private final HashCacheExecutorProperties props;

    /*
        Executor is managed as a Spring bean with proper lifecycle shutdown
        to avoid thread leaks in tests and containers.
        CallerRunsPolicy is used to avoid losing refill tasks under load.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService hashCacheExecutor() {
        ThreadFactory threadFactory = namedDaemonThreadFactory();

        return new ThreadPoolExecutor(
                props.getPoolSize(),
                props.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(props.getQueueCapacity()),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // run refill tasks on the caller thread
        );
    }

    private static ThreadFactory namedDaemonThreadFactory() {
        AtomicInteger seq = new AtomicInteger(1);
        return runnable -> {
            Thread t = new Thread(runnable);
            t.setName(HashCacheExecutorConfig.THREAD_NAME_PREFIX + seq.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }
}