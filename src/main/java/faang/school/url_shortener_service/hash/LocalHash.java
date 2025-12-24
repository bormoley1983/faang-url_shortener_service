package faang.school.url_shortener_service.hash;

import faang.school.url_shortener_service.entity.Hash;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
public class LocalHash {

    @Value("${faang.school.url-shortener-service.threshold}")
    private int threshold;

    @Value("${faang.school.url-shortener-service.refillBatchSize}")
    private int refillBatchSize;

    private final Queue<Hash> localHash = new ConcurrentLinkedQueue<>();
    private final HashGenerator hashGenerator;

    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        hashGenerator.generateHash();
        triggerRefill();
    }

    public Hash getHash() {
        Hash hash = localHash.poll();

        if (localHash.size() < threshold) {
            triggerRefill();
        }
        return hash;
    }

    private void triggerRefill() {
        // Пытаемся захватить "флаг" генерации
        if (isRefilling.compareAndSet(false, true)) {
            try {
                log.info("Local hash buffer low ({} left), refilling {} hashes...", localHash.size(), refillBatchSize);
                refillInternal();
            } finally {
                isRefilling.set(false);
            }
        }
        // Если другой поток уже генерирует — ничего не делаем (ждём)
    }

    private void refillInternal() {
        for (int i = 0; i < refillBatchSize; i++) {
            try {
                List<Hash> newHash = hashGenerator.getHash(); // ← ваш метод
                localHash.addAll(newHash);
            } catch (Exception e) {
                log.error("Failed to generate hash during refill", e);
                // Можно прервать или продолжить — зависит от требований
                break;
            }
        }
        log.info("Refill completed. Buffer size: {}", localHash.size());
    }
}