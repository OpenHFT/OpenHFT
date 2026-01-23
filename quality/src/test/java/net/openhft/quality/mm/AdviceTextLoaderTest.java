/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdviceTextLoader}.
 */
@DisplayName("Advice text loader tests")
class AdviceTextLoaderTest {

    @Test
    @DisplayName("Load advice text from resource")
    void loadAdviceTextFromResource() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);

        assertNotNull(text.title(), "title should be loaded");
        assertNotEquals(text.intentIntro(), text.intentOutro(),
                "intro and outro should differ");
        assertNotEquals(text.hintA(), text.hintB(),
                "hints should differ");
    }
}
