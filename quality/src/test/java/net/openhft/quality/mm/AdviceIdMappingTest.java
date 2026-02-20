/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("AdviceId mapping tests")
class AdviceIdMappingTest {

    @Test
    @DisplayName("Manual advice ids map to rule and source")
    void manualAdviceIdsMapToRuleAndSource() {
        assertManualMapping(AdviceId.MMAnnotationDisplayNameMissing);
        assertManualMapping(AdviceId.MMAnnotationTestOrder);
        assertManualMapping(AdviceId.MMAnnotationJUnit4Annotation);
        assertManualMapping(AdviceId.MMAnnotationJUnit4Assertion);
        assertManualMapping(AdviceId.MMCommentMapStringObject);
    }

    @Test
    @DisplayName("File-level and unhandled advice ids map to rule")
    void fileLevelAndUnhandledAdviceIdsMapToRule() {
        assertRuleOnlyMapping(AdviceId.MMOverusedWord);
        assertRuleOnlyMapping(AdviceId.MMLacksPurpose);
        assertRuleOnlyMapping(AdviceId.MMLowEntropy);
        assertRuleOnlyMapping(AdviceId.MMUnhandled);
    }

    @Test
    @DisplayName("Assertion-only rules return UNKNOWN for other sources")
    void assertionOnlyRulesReturnUnknownForOtherSources() {
        assertUnknownForNonAssertionSources(RuleId.MISSING_COMPARISON_VALUES);
        assertUnknownForNonAssertionSources(RuleId.MISSING_STRING_VALUE);
    }

    private void assertManualMapping(AdviceId adviceId) {
        RuleId ruleId = adviceId.ruleId();
        AdviceSource source = adviceId.adviceSource();
        assertNotNull(ruleId, "ruleId should be set for " + adviceId);
        assertNotNull(source, "adviceSource should be set for " + adviceId);
        assertEquals(adviceId, AdviceId.forRule(ruleId, source),
                "Expected mapping for " + ruleId + " and " + source);
    }

    private void assertRuleOnlyMapping(AdviceId adviceId) {
        RuleId ruleId = adviceId.ruleId();
        assertNotNull(ruleId, "ruleId should be set for " + adviceId);
        assertNull(adviceId.adviceSource(), "adviceSource should be null for " + adviceId);
        assertEquals(adviceId, AdviceId.forRule(ruleId),
                "Expected rule-only mapping for " + ruleId);
    }

    private void assertUnknownForNonAssertionSources(RuleId ruleId) {
        AdviceSource[] sources = {
                AdviceSource.PRECONDITION,
                AdviceSource.THROW,
                AdviceSource.LOG,
                AdviceSource.COMMENT
        };
        for (AdviceSource source : sources) {
            assertEquals(AdviceId.UNKNOWN, AdviceId.forRule(ruleId, source),
                    "Expected UNKNOWN for " + ruleId + " with " + source);
        }
    }
}
