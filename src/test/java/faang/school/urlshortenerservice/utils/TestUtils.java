package faang.school.urlshortenerservice.utils;

import faang.school.urlshortenerservice.service.HashCacheService;

import java.lang.reflect.Field;
import java.util.List;

public class TestUtils {
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void offerHashToCache(HashCacheService service, String hash) {
        setField(service, "hashQueue", new java.util.concurrent.ConcurrentLinkedQueue<>(List.of(hash)));
    }
}
