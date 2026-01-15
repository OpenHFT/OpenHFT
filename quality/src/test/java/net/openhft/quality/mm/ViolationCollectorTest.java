/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Violation collector tests scenario case")
class ViolationCollectorTest {

    @Test
    @DisplayName("Flush skips null entries without logging")
    void flushSkipsNullEntriesWithoutLogging() throws Exception {
        ViolationCollector collector = new ViolationCollector(null);
        collector.putPendingForTesting(5, null);
        collector.putPendingForTesting(6, new Violation(6, RuleId.MISSING_MESSAGE, new Object[]{}));

        TestCheck check = new TestCheck();
        check.configure(new com.puppycrawl.tools.checkstyle.DefaultConfiguration("TestCheck"));
        collector.flush(check);

        assertEquals(1, check.getViolations().size(),
                "flush should log only non-null violations");
    }

    @Test
    @DisplayName("Priority comparison uses order when lengths match")
    void priorityComparisonUsesOrderWhenLengthsMatch() {
        assertTrue(ViolationCollector.isHigherPriority(1, "AA", 1, 1, "BB", 2),
                "lower order should win when priority and code length match");
        assertFalse(ViolationCollector.isHigherPriority(1, "AA", 3, 1, "BB", 2),
                "higher order should not win when priority and code length match");
    }

    private static final class TestCheck extends AbstractCheck {

        @Override
        public int[] getDefaultTokens() {
            return new int[0];
        }

        @Override
        public int[] getAcceptableTokens() {
            return new int[0];
        }

        @Override
        public int[] getRequiredTokens() {
            return new int[0];
        }
    }
}
