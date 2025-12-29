package net.openhft.quality.checkstyle.inputs;

// Fake Test annotation, not JUnit5
@interface Test {}

class InputNonJUnit5Test {
    @Test
    void testSomething() {
    }
}
