package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashCleanerServiceTest {
    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HashCleanerService hashCleanerService;

    @Test
    void cleanupOutdatedHashes_shouldDeleteExpiredUrlsAndMoveHashesToHashTable() {
        List<String> retrievedHashes = List.of("abc123", "def456", "ghi789");
        when(urlRepository.deleteExpiredUrlsAndReturnHashes()).thenReturn(retrievedHashes);

        hashCleanerService.cleanupOutdatedHashes();

        verify(urlRepository).deleteExpiredUrlsAndReturnHashes();
        // hashes are recycled idempotently via JdbcTemplate (ON CONFLICT DO NOTHING)
        verify(jdbcTemplate).update(anyString(), eq("abc123"));
        verify(jdbcTemplate).update(anyString(), eq("def456"));
        verify(jdbcTemplate).update(anyString(), eq("ghi789"));

        verify(urlCacheRepository).deleteByHash("abc123");
        verify(urlCacheRepository).deleteByHash("def456");
        verify(urlCacheRepository).deleteByHash("ghi789");
    }

    @Test
    void cleanupOutdatedHashes_shouldNotSaveHashesWhenNoUrlsFound() {
        List<String> emptyList = List.of();
        when(urlRepository.deleteExpiredUrlsAndReturnHashes()).thenReturn(emptyList);

        hashCleanerService.cleanupOutdatedHashes();

        verify(urlRepository).deleteExpiredUrlsAndReturnHashes();
        verify(jdbcTemplate, never()).update(anyString(), (Object) anyString());
        verifyNoInteractions(urlCacheRepository);
    }
}
