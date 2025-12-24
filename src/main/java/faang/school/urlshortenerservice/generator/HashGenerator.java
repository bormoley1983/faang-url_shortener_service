package faang.school.urlshortenerservice.generator;

import java.util.concurrent.CompletableFuture;

public interface HashGenerator {
    CompletableFuture<Void> generateBatch();
}
