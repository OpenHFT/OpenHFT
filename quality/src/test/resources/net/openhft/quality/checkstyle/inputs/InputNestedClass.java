package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Outer class")
class InputNestedClass {
    class NestedTest { // violation
        @Test
        void testNested() { // violation
        }
    }
}
