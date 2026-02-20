package net.openhft.quality.checkstyle.inputs;

// Test annotation that starts with "Test" but is not JUnit 5
@interface TestMarker {
}

// Annotation that looks like FQN but is not JUnit
class InputNonJUnit5FqnAnnotation {
    @com.example.Test
    void testSomething() {
    }
}
