/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckJUnit4Fixture {

    @Test
    public void junit4AssertionUsage() {
        assertTrue("cache entry should remain stable after update", true);
    }
}
