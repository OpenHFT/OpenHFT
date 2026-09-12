package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.RepeatedTest;

class InputRepeatedTestWithoutDisplayName { // violation
    @RepeatedTest(3)
    void testSomething() { // violation
    }
}
