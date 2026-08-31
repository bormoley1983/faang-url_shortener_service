package faang.school.urlshortenerservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashPoolServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HashPoolService hashPoolService;

    @Test
    void takeBatch_shouldReturnHashesFromDatabase() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyInt()))
                .thenReturn(List.of("AAA001", "AAA002"));

        List<String> result = hashPoolService.takeBatch(2);

        assertEquals(List.of("AAA001", "AAA002"), result);
        verify(jdbcTemplate).queryForList(anyString(), eq(String.class), eq(2));
    }

    @Test
    void takeBatch_shouldReturnEmptyList_whenNoHashesAvailable() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyInt()))
                .thenReturn(List.of());

        List<String> result = hashPoolService.takeBatch(10);

        assertTrue(result.isEmpty());
    }
}
