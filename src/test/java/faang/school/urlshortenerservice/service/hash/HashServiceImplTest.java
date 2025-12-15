package faang.school.urlshortenerservice.service.hash;

import faang.school.urlshortenerservice.generator.HashGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashServiceImplTest {

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HashServiceImpl hashService;

    private static final int BATCH_SIZE = 50;

    @Test
    void generateHash_delegatesToGeneratorAndSavesInBatches() {
        List<String> generatedHashes = List.of("abc1", "abc2", "abc3", "abc4", "abc5");
        when(hashGenerator.generateHash()).thenReturn(generatedHashes);

        ReflectionTestUtils.setField(hashService, "batchSize", BATCH_SIZE);

        hashService.generateHash();

        verify(hashGenerator).generateHash();
        verify(jdbcTemplate, times(1))
                .batchUpdate(eq("INSERT INTO hash (hash) VALUES (?)"), any(BatchPreparedStatementSetter.class));

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        BatchPreparedStatementSetter setter = captor.getValue();
        assertThat(setter.getBatchSize()).isEqualTo(5);
    }

    @Test
    void getHashes_delegatesToHashGenerator() {
        long hashLimit = 10L;
        List<String> expectedHashes = List.of("h1", "h2", "h3");
        when(hashGenerator.getHashes(hashLimit)).thenReturn(expectedHashes);

        List<String> result = hashService.getHashes(hashLimit);

        assertThat(result).isEqualTo(expectedHashes);
        verify(hashGenerator).getHashes(hashLimit);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void saveHashByBatch_oneBatch_whenListSmallerThanBatchSize() {
        List<String> hashes = List.of("a", "b", "c");
        ReflectionTestUtils.setField(hashService, "batchSize", BATCH_SIZE);

        hashService.saveHashByBatch(hashes);

        verify(jdbcTemplate, times(1))
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        BatchPreparedStatementSetter setter = captor.getValue();
        assertThat(setter.getBatchSize()).isEqualTo(3);
    }

    @Test
    void saveHashByBatch_exactMultipleOfBatchSize_multipleBatches() {
        List<String> hashes = generateHashesList(BATCH_SIZE * 3); // ровно 3 батча
        ReflectionTestUtils.setField(hashService, "batchSize", BATCH_SIZE);

        hashService.saveHashByBatch(hashes);

        verify(jdbcTemplate, times(3))
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate, times(3)).batchUpdate(anyString(), captor.capture());

        List<BatchPreparedStatementSetter> setters = captor.getAllValues();
        assertThat(setters).hasSize(3);
        setters.forEach(setter -> assertThat(setter.getBatchSize()).isEqualTo(BATCH_SIZE));
    }

    @Test
    void saveHashByBatch_moreThanBatchSize_lastBatchSmaller() {
        int totalHashes = BATCH_SIZE * 2 + 17; // 2 полных батча + один неполный
        List<String> hashes = generateHashesList(totalHashes);
        ReflectionTestUtils.setField(hashService, "batchSize", BATCH_SIZE);

        hashService.saveHashByBatch(hashes);

        verify(jdbcTemplate, times(3))
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate, times(3)).batchUpdate(anyString(), captor.capture());

        List<BatchPreparedStatementSetter> setters = captor.getAllValues();
        assertThat(setters)
                .hasSize(3)
                .extracting(BatchPreparedStatementSetter::getBatchSize)
                .containsExactly(BATCH_SIZE, BATCH_SIZE, 17);
    }

    @Test
    void saveHashByBatch_emptyList_noBatchUpdateCalled() {
        List<String> hashes = List.of();
        ReflectionTestUtils.setField(hashService, "batchSize", BATCH_SIZE);

        hashService.saveHashByBatch(hashes);

        verifyNoInteractions(jdbcTemplate);
    }

    private List<String> generateHashesList(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> "hash" + i)
                .toList();
    }
}