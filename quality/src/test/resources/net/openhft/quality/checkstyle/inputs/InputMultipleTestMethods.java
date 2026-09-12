package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.Test;

class InputMultipleTestMethods { // violation
    @Test
    void test1() { // violation
    }

    @Test
    void test2() { // violation
    }
}
