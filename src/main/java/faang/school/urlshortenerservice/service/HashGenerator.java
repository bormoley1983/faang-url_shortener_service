package faang.school.urlshortenerservice.service;

import java.util.concurrent.CompletableFuture;

public interface HashGenerator {
    CompletableFuture<Integer> generateBatch();
}
