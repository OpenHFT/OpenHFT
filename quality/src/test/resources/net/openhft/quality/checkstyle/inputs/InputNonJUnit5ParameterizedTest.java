package net.openhft.quality.checkstyle.inputs;

// Custom ParameterizedTest annotation - NOT from JUnit5
@interface ParameterizedTest {
}

class InputNonJUnit5ParameterizedTest {
    @ParameterizedTest
    void testSomething() {
    }
}
