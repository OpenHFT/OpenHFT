/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("MMDisplayName")
@DisplayName("Self check fixture tests because coverage must show failures")
public class SelfCheckFixtureTest {

    @Test
    @DisplayName("Test fixture usage scenario case detail")
    public void testFixtureUsage() {
        SelfCheckFixture fixture = new SelfCheckFixture();
        String value = "value";

        fixture.triggerRestatesDerived(value);
        fixture.triggerContextless(value);
        fixture.triggerIndexOnly(value);
        fixture.triggerLongWord(value);
        fixture.triggerTrivialSupplier(value);
        fixture.triggerGeneric(value);
        fixture.triggerRedundantClass(true);
        fixture.triggerRedundantMethod(true);
        fixture.triggerRedundantLine(true);
        fixture.triggerRestatesAssertion(true);
        fixture.triggerWhitespaceRun(value);
        fixture.triggerTooShort(value);
        fixture.triggerTooLong(value);
        fixture.triggerTooFewMeaningful(value);
        fixture.triggerMissingSubjectLog();
        fixture.triggerDuplicateFirst(value);
        fixture.triggerDuplicateSecond(value);

        assertThrows(IllegalStateException.class, fixture::triggerMissingMessage,
                "triggerMissingMessage should throw IllegalStateException");
    }
}
