/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic Jupiter smoke to ensure JUnit 5 assertions execute properly.
 */
class JupiterSmokeTest {

    @Test
    @DisplayName("JUnit Jupiter should run basic assertions")
    void jupiterRunsBasicTest() {
        assertEquals(2, 1 + 1, "Jupiter core assertion should work");
    }
}
