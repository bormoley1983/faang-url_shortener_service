package faang.school.urlshortenerservice.service.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncService {

    CompletableFuture<List<String>> getHashesAsync(long amount);
}