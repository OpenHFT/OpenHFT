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

    @Test
    @DisplayName("Every AdviceId except UNKNOWN has intent and verbose entries")
    void everyAdviceIdHasIntentAndVerboseEntries() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);

        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            AdviceText text = loader.textFor(adviceId);
            assertNotNull(text.intentIntro(),
                    adviceId.name() + " should have intent_intro in mm-advice.properties");
            assertFalse(text.intentIntro().isEmpty(),
                    adviceId.name() + " intent_intro should not be empty");
            assertNotNull(text.verbose(),
                    adviceId.name() + " should have verbose entry in mm-advice.properties");
            assertFalse(text.verbose().isEmpty(),
                    adviceId.name() + " verbose should not be empty");
        }
    }
}
