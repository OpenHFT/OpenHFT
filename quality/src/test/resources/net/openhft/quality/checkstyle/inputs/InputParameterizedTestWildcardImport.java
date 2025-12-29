package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.ValueSource;

class InputParameterizedTestWildcardImport { // violation
    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    void testSomething(String value) { // violation
    }
}
