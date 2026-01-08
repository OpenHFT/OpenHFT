/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Rule id tests scenario case detail")
class RuleIdTest {

    @Test
    @DisplayName("Applies to null source returns false scenario case")
    void appliesToNullSourceReturnsFalse() {
        assertFalse(RuleId.TOO_SHORT.appliesTo(null));
    }

    @Test
    @DisplayName("Applies to known source returns true scenario")
    void appliesToKnownSourceReturnsTrue() {
        assertTrue(RuleId.TOO_SHORT.appliesTo(MessageSource.ASSERTION));
    }
}
