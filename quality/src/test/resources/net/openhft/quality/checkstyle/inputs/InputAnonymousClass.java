package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test with anonymous class")
class InputAnonymousClass {
    @Test
    @DisplayName("Test that creates anonymous class")
    void testWithAnonymousClass() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
            }
        };
    }
}
