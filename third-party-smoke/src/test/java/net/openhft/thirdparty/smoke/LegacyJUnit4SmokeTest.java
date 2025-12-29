/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.Assert;
import org.junit.Test;

public class LegacyJUnit4SmokeTest {

    /**
     * Confirms the Vintage engine can execute a JUnit 4 test.
     */
    @Test
    public void vintageEngineRuns() {
        Assert.assertTrue("Vintage engine should execute JUnit4 test", true);
    }
}
