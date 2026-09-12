package net.openhft.quality.checkstyle.inputs;

class InputFqnTestAnnotation { // violation
    @org.junit.jupiter.api.Test
    void testSomething() { // violation
    }
}
