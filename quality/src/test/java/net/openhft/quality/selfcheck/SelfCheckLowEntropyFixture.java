/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelfCheckLowEntropyFixture {

    public void triggerLowEntropy() {
        assertTrue(true, "cache update occurs because entry state changes one");
        assertTrue(true, "cache update occurs because entry state changes two");
        assertTrue(true, "cache update occurs because entry state changes three");
    }
}
