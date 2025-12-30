package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test with static import")
class InputStaticImport {
    @Test
    @DisplayName("Uses static import")
    void testWithStaticImport() {
        assertEquals(1, 1);
    }
}
