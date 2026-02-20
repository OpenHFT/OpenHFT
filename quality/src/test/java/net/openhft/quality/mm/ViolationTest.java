/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    @DisplayName("Violation constructed with null args returns null from args()")
    void args_nullArgsReturnsNull() {
        Violation violation = new Violation(5, RuleId.GENERIC, null);
        assertNull(violation.args(), "args() should return null when constructed with null args");
        assertEquals(5, violation.lineNo(), "lineNo should be preserved");
        assertEquals(RuleId.GENERIC, violation.ruleId(), "ruleId should be preserved");
    }

    @Test
    @DisplayName("Violation toString includes all fields")
    void toString_includesAllFields() {
        Violation violation = new Violation(10, RuleId.TOO_SHORT, new Object[]{"test"});
        String str = violation.toString();
        assertNotNull(str, "toString() should not return null");
    }
}
