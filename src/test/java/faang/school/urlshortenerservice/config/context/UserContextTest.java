package faang.school.urlshortenerservice.config.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserContextTest {

    @Test
    void getUserId_shouldReturnSetUserId() {
        UserContext userContext = new UserContext();

        userContext.setUserId(42L);

        assertEquals(42L, userContext.getUserId());
    }

    @Test
    void clear_shouldRemoveUserId() {
        UserContext userContext = new UserContext();
        userContext.setUserId(42L);

        userContext.clear();

        // getUserId() returns primitive long; after clear the internal Long is null,
        // so unboxing throws NPE — that is the observable "cleared" state
        assertThrows(NullPointerException.class, userContext::getUserId);
    }
}
