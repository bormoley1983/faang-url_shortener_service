package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HashCacheService {
    public String getHash() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}
