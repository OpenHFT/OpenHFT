/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ViolationTest {

    @Test
    void args_areDefensivelyCopied() {
        Object[] original = {"first", 42};
        Violation violation = new Violation(12, RuleId.GENERIC, original);

        Object[] firstRead = violation.args();
        assertNotSame(original, firstRead);
        assertEquals("first", firstRead[0]);

        original[0] = "changed";
        assertEquals("first", violation.args()[0]);

        firstRead[0] = "mutated";
        assertEquals("first", violation.args()[0]);
    }
}
