package faang.school.urlshortenerservice.service.async;

import faang.school.urlshortenerservice.service.hash.HashService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class AsyncServiceImpl implements AsyncService {
    private final HashService hashService;

    @Async(value = "threadExecutor")
    public CompletableFuture<List<String>> getHashesAsync(long amount) {
        return CompletableFuture.completedFuture(hashService.getHashes(amount));
    }
}