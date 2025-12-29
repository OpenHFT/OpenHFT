package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Parameterized test class")
class InputParameterizedTestWithDisplayName {
    @ParameterizedTest
    @DisplayName("Parameterized test")
    @ValueSource(strings = {"a", "b"})
    void testSomething(String value) {
    }
}
