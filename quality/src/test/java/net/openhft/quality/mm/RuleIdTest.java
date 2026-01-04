/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleIdTest {

    @Test
    void appliesToNullSourceReturnsFalse() {
        assertFalse(RuleId.TOO_SHORT.appliesTo(null));
    }

    @Test
    void appliesToKnownSourceReturnsTrue() {
        assertTrue(RuleId.TOO_SHORT.appliesTo(MessageSource.ASSERTION));
    }
}
