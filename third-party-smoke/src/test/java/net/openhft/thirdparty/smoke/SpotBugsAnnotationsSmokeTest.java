/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying SpotBugs annotations are available at runtime for static analysis.
 * <p>
 * Tests that the spotbugs-annotations dependency is correctly resolved and that
 * annotations can be used for nullability documentation and warning suppression.
 * Note: The legacy NonNull and Nullable annotations are deprecated in favor of JSR-305/JSpecify,
 * so this test uses reflection to verify their presence without importing them directly.
 */
@DisplayName("Smoke test verifies SpotBugs annotations are available")
class SpotBugsAnnotationsSmokeTest {

    private static final String NON_NULL_CLASS = "edu.umd.cs.findbugs.annotations.NonNull";
    private static final String NULLABLE_CLASS = "edu.umd.cs.findbugs.annotations.Nullable";

    @Test
    @DisplayName("SpotBugs @NonNull annotation class should be loadable from classpath via reflection")
    void spotBugsNonNullAnnotationIsPresent() throws Exception {
        Class<?> nonNullAnnotation = Class.forName(NON_NULL_CLASS);

        assertNotNull(nonNullAnnotation,
                "SpotBugs NonNull annotation class should be loadable from classpath");
        assertTrue(nonNullAnnotation.isAnnotation(),
                "edu.umd.cs.findbugs.annotations.NonNull should be an annotation type");
    }

    @Test
    @DisplayName("SpotBugs @Nullable annotation class should be loadable from classpath via reflection")
    void spotBugsNullableAnnotationIsPresent() throws Exception {
        Class<?> nullableAnnotation = Class.forName(NULLABLE_CLASS);

        assertNotNull(nullableAnnotation,
                "SpotBugs Nullable annotation class should be loadable from classpath");
        assertTrue(nullableAnnotation.isAnnotation(),
                "edu.umd.cs.findbugs.annotations.Nullable should be an annotation type");
    }

    @Test
    @DisplayName("SpotBugs @SuppressFBWarnings annotation class should be loadable and usable")
    void spotBugsSuppressFBWarningsAnnotationIsPresent() {
        // SuppressFBWarnings has CLASS retention, so we verify the class is loadable
        // and has the expected annotation structure for compile-time processing
        assertNotNull(SuppressFBWarnings.class.getName(),
                "SpotBugs SuppressFBWarnings annotation class should be loadable from classpath");
        assertTrue(SuppressFBWarnings.class.isAnnotation(),
                "edu.umd.cs.findbugs.annotations.SuppressFBWarnings should be an annotation type");

        // Verify the annotation has expected methods for value and justification
        try {
            assertNotNull(SuppressFBWarnings.class.getMethod("value"),
                    "SuppressFBWarnings should have a value() method for warning codes");
            assertNotNull(SuppressFBWarnings.class.getMethod("justification"),
                    "SuppressFBWarnings should have a justification() method for explanation");
        } catch (NoSuchMethodException e) {
            fail("SuppressFBWarnings annotation should have value() and justification() methods: " + e);
        }
    }
}
