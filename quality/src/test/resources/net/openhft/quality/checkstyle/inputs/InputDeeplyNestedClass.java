package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Outer class")
class InputDeeplyNestedClass {
    @DisplayName("Inner1")
    class Inner1 {
        @DisplayName("Inner2")
        class Inner2 {
            @Test
            @DisplayName("Deeply nested test")
            void deepTest() {
            }
        }
    }
}
