package net.openhft.quality.checkstyle.inputs;

class InputFqnParameterizedTest { // violation
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"a"})
    void testSomething(String value) { // violation
    }
}
