package net.openhft.quality.checkstyle.inputs;

// No imports - if imports from previous file leak, @Test would be recognized as JUnit5
class InputSecondFileAfterImports {
    @Test
        // Should NOT be recognized as JUnit5 (no import)
    void testSomething() {
    }
}
