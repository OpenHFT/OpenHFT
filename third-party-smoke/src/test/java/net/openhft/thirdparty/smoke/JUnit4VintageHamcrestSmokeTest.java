/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Vintage engine smoke to ensure JUnit 4 and Hamcrest remain usable.
 */
public class JUnit4VintageHamcrestSmokeTest {

    /**
     * Expected sum used by the vintage JUnit assertion check.
     */
    private static final int EXPECTED_SUM = 4;

    /**
     * Value used in Hamcrest matcher assertions.
     */
    private static final int MEANING_OF_LIFE = 42;

    /**
     * Lower bound used in Hamcrest matcher assertions.
     */
    private static final int LOWER_BOUND = 10;

    /**
     * Ensures basic JUnit 4 execution via the vintage engine.
     */
    @Test
    public void vintageRunsBasicTest() {
        assertEquals("JUnit4 vintage engine should execute assertions", EXPECTED_SUM, 2 + 2);
    }

    /**
     * Ensures Hamcrest matchers run under the vintage engine.
     */
    @Test
    public void hamcrestMatchersWork() {
        MatcherAssert.assertThat("Hamcrest matcher should verify string prefix correctly",
                "hello", Matchers.startsWith("he"));
        MatcherAssert.assertThat("Hamcrest matcher should verify numeric comparison correctly",
                MEANING_OF_LIFE, Matchers.greaterThan(LOWER_BOUND));
    }
}
