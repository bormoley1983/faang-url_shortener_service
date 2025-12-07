package faang.school.urlshortenerservice.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для генерации и помещения новых хешей в БД
 */
public interface HashGenerator {

    /**
     * Генерирует партию хэшей и сохраняет их в БД
     *
     * @return future со списком сгенерированных хешей
     */
    CompletableFuture<List<String>> generateBatch();
}
