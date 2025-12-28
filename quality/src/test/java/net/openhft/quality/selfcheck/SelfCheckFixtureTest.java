/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import org.junit.Assert;
import org.junit.Test;

public class SelfCheckFixtureTest {

    @Test
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

        try {
            fixture.triggerMissingMessage();
            Assert.fail("Expected IllegalStateException from triggerMissingMessage invocation");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}
