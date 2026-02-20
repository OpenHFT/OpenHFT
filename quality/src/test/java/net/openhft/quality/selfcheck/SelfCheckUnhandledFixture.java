/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import org.junit.jupiter.api.DisplayName;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckUnhandledFixture {
    private static final String NAME = "non literal display name";

    @DisplayName(NAME)
    public void unhandledAnnotationValue() {
    }
}
