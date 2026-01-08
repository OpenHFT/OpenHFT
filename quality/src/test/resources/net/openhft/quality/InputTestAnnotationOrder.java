/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test input for @Test annotation ordering.
 */
@SuppressWarnings({"MMTooShort", "MMTooFewMeaningfulWords"})
class InputTestAnnotationOrder {

    @DisplayName("Annotation order violation")
    @Test
    void testAnnotationOrderViolation() {
    }

    @Test
    @DisplayName("Annotation order ok")
    void testAnnotationOrderOk() {
    }
}
