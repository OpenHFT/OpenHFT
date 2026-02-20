/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Self check display name method fixture")
class SelfCheckMissingDisplayNameMethodFixture {

    @Test
    void missingDisplayNameMethod() {
    }
}

class SelfCheckMissingDisplayNameClassFixture {

    @Test
    @DisplayName("method has display name")
    void classMissingDisplayName() {
    }
}
