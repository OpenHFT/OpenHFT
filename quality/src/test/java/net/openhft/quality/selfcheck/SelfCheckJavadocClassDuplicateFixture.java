/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateMessageSeed {
    public void seedDuplicateMessage() {
        assertTrue(true, "duplicate javadoc class message");
    }
}

/**
 * duplicate javadoc class message
 */
class SelfCheckJavadocClassDuplicateFixture {
}
