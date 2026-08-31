package faang.school.urlshortenerservice.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueValuesListValidatorTest {

    @Test
    void validateList_shouldPass_whenListHasValues() {
        assertDoesNotThrow(() -> UniqueValuesListValidator.validateList(List.of(1L, 2L), "empty"));
    }

    @Test
    void validateList_shouldThrow_whenListNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UniqueValuesListValidator.validateList(null, "list is null!"));

        assertEquals("list is null!", exception.getMessage());
    }

    @Test
    void validateList_shouldThrow_whenListEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UniqueValuesListValidator.validateList(List.of(), "list is empty!"));

        assertEquals("list is empty!", exception.getMessage());
    }

    @Test
    void validateUniqueness_shouldPass_whenAllValuesUnique() {
        assertDoesNotThrow(() -> UniqueValuesListValidator.validateUniqueness(List.of(1L, 2L, 3L)));
    }

    @Test
    void validateUniqueness_shouldThrow_whenDuplicatesPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UniqueValuesListValidator.validateUniqueness(List.of(1L, 2L, 1L)));

        assertEquals("Supplied list contains duplicate values!", exception.getMessage());
    }
}
