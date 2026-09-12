/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RuleRegistry} including round-trip lookups and uniqueness.
 */
@DisplayName("Rule registry tests for code lookup and uniqueness")
class RuleRegistryTest {

    @Test
    @DisplayName("forCode round-trip returns original RuleId for every enum constant")
    void forCodeRoundTrip() {
        for (RuleId ruleId : RuleId.values()) {
            RuleId resolved = RuleRegistry.forCode(ruleId.code());
            assertSame(ruleId, resolved,
                    "Round-trip for " + ruleId.name() + " (code=" + ruleId.code() + ") should return same instance");
        }
    }

    @Test
    @DisplayName("All RuleId codes are unique")
    void allCodesAreUnique() {
        Set<String> codes = new HashSet<>();
        for (RuleId ruleId : RuleId.values()) {
            assertTrue(codes.add(ruleId.code()),
                    "Duplicate code '" + ruleId.code() + "' found on " + ruleId.name());
        }
    }

    @Test
    @DisplayName("forCode returns null for unknown code")
    void forCodeUnknownReturnsNull() {
        assertNull(RuleRegistry.forCode("NoSuchCode"),
                "Unknown code should return null");
    }

    @Test
    @DisplayName("forCode with null throws NPE")
    void forCodeNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> RuleRegistry.forCode(null),
                "Null code should throw NPE");
    }
}
