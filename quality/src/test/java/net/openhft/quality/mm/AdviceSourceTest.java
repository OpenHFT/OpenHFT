/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AdviceSource} enum to improve branch coverage.
 */
@DisplayName("Advice source tests")
class AdviceSourceTest {

    @ParameterizedTest
    @EnumSource(AdviceSource.class)
    @DisplayName("idPrefix returns non-null for all enum values")
    void idPrefix_returnsNonNullForAllValues(AdviceSource source) {
        String prefix = source.idPrefix();
        assertNotNull(prefix, "idPrefix() should not return null for " + source);
    }

    @Test
    @DisplayName("idPrefix returns correct values for code construct sources")
    void idPrefix_correctValuesForCodeConstructSources() {
        assertEquals("Assertion", AdviceSource.ASSERTION.idPrefix(), "ASSERTION prefix");
        assertEquals("Precondition", AdviceSource.PRECONDITION.idPrefix(), "PRECONDITION prefix");
        assertEquals("Throw", AdviceSource.THROW.idPrefix(), "THROW prefix");
        assertEquals("Log", AdviceSource.LOG.idPrefix(), "LOG prefix");
        assertEquals("Comment", AdviceSource.COMMENT.idPrefix(), "COMMENT prefix");
        assertEquals("JavadocClass", AdviceSource.JAVADOC_CLASS.idPrefix(), "JAVADOC_CLASS prefix");
        assertEquals("JavadocMember", AdviceSource.JAVADOC_MEMBER.idPrefix(), "JAVADOC_MEMBER prefix");
    }

    @Test
    @DisplayName("idPrefix returns correct values for annotation subtypes")
    void idPrefix_correctValuesForAnnotationSubtypes() {
        assertEquals("AnnotationDisplayName", AdviceSource.ANNOTATION_DISPLAY_NAME.idPrefix(),
                "ANNOTATION_DISPLAY_NAME prefix");
        assertEquals("AnnotationDisabled", AdviceSource.ANNOTATION_DISABLED.idPrefix(),
                "ANNOTATION_DISABLED prefix");
        assertEquals("AnnotationTestOrder", AdviceSource.ANNOTATION_TEST_ORDER.idPrefix(),
                "ANNOTATION_TEST_ORDER prefix");
        assertEquals("AnnotationJUnit4", AdviceSource.ANNOTATION_JUNIT4.idPrefix(),
                "ANNOTATION_JUNIT4 prefix");
    }

    @Test
    @DisplayName("fromMessageSource returns null for null input")
    void fromMessageSource_returnsNullForNullInput() {
        assertNull(AdviceSource.fromMessageSource(null), "null input should return null");
    }

    @Test
    @DisplayName("fromMessageSource maps code construct sources correctly")
    void fromMessageSource_mapsCodeConstructSourcesCorrectly() {
        assertEquals(AdviceSource.ASSERTION, AdviceSource.fromMessageSource(MessageSource.ASSERTION),
                "ASSERTION mapping");
        assertEquals(AdviceSource.PRECONDITION, AdviceSource.fromMessageSource(MessageSource.PRECONDITION),
                "PRECONDITION mapping");
        assertEquals(AdviceSource.THROW, AdviceSource.fromMessageSource(MessageSource.THROW),
                "THROW mapping");
        assertEquals(AdviceSource.LOG, AdviceSource.fromMessageSource(MessageSource.LOG),
                "LOG mapping");
        assertEquals(AdviceSource.COMMENT, AdviceSource.fromMessageSource(MessageSource.COMMENT),
                "COMMENT mapping");
        assertEquals(AdviceSource.JAVADOC_CLASS, AdviceSource.fromMessageSource(MessageSource.JAVADOC_CLASS),
                "JAVADOC_CLASS mapping");
        assertEquals(AdviceSource.JAVADOC_MEMBER, AdviceSource.fromMessageSource(MessageSource.JAVADOC_MEMBER),
                "JAVADOC_MEMBER mapping");
    }

    @Test
    @DisplayName("fromMessageSource returns null for ANNOTATION source")
    void fromMessageSource_returnsNullForAnnotationSource() {
        assertNull(AdviceSource.fromMessageSource(MessageSource.ANNOTATION),
                "ANNOTATION should return null (requires explicit advice source)");
    }

    @ParameterizedTest
    @EnumSource(MessageSource.class)
    @DisplayName("fromMessageSource handles all MessageSource values without exception")
    void fromMessageSource_handlesAllMessageSourceValues(MessageSource source) {
        // Should not throw for any MessageSource value
        AdviceSource result = AdviceSource.fromMessageSource(source);
        // Result can be null (for ANNOTATION) or non-null (for mapped sources)
        if (source == MessageSource.ANNOTATION) {
            assertNull(result, "ANNOTATION should map to null");
        } else {
            assertNotNull(result, source + " should map to a non-null AdviceSource");
        }
    }

    @Test
    @DisplayName("All AdviceSource values have distinct idPrefix values")
    void allAdviceSourceValues_haveDistinctIdPrefixValues() {
        java.util.Set<String> prefixes = new java.util.HashSet<>();
        for (AdviceSource source : AdviceSource.values()) {
            String prefix = source.idPrefix();
            boolean added = prefixes.add(prefix);
            if (!added) {
                throw new AssertionError("Duplicate idPrefix found: " + prefix);
            }
        }
        assertEquals(AdviceSource.values().length, prefixes.size(),
                "All idPrefix values should be unique");
    }

    @Test
    @DisplayName("Enum values count matches expected")
    void enumValues_countMatchesExpected() {
        // 7 code construct sources + 4 annotation subtypes = 11 total
        assertEquals(11, AdviceSource.values().length, "Expected 11 AdviceSource values");
    }
}
