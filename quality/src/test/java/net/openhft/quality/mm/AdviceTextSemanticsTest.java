/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Advice text semantics tests")
class AdviceTextSemanticsTest {
    private final AdviceTextLoader loader = AdviceTextLoader.loadFromResource(
            AdviceReportManager.ADVICE_TEXT_RESOURCE);

    @Test
    @DisplayName("Comment advice avoids failure-centric language")
    void commentAdviceAvoidsFailureCentricLanguage() {
        assertNoToken(AdviceId.MMCommentMessageMissing, "fail");
        assertNoToken(AdviceId.MMCommentMessageMissing, "expected vs actual");
        assertNoToken(AdviceId.MMCommentMessageMissing, "triage");

        assertNoToken(AdviceId.MMCommentMessageMissingSubject, "fail");

        assertNoToken(AdviceId.MMCommentMessageTooShort, "triage");
        assertNoToken(AdviceId.MMCommentMessageTooShort, "expected vs actual");

        assertNoToken(AdviceId.MMCommentMessageTooFewMeaningfulWords, "expected vs actual");
    }

    @Test
    @DisplayName("Javadoc advice avoids stack trace and triage wording")
    void javadocAdviceAvoidsStackTraceAndTriageWording() {
        assertNoToken(AdviceId.MMJavadocClassMessageRedundantClassName, "stack trace");
        assertNoToken(AdviceId.MMJavadocMemberMessageRedundantMethodName, "stack trace");

        assertNoToken(AdviceId.MMJavadocClassMessageTooShort, "triage");
        assertNoToken(AdviceId.MMJavadocMemberMessageTooShort, "triage");
    }

    private void assertNoToken(AdviceId adviceId, String token) {
        AdviceText text = loader.textFor(adviceId);
        String normalizedToken = token.toLowerCase(Locale.ROOT);
        assertFalse(normalize(text.title()).contains(normalizedToken),
                adviceId + " title should not include " + token);
        assertFalse(normalize(text.intentIntro()).contains(normalizedToken),
                adviceId + " intro should not include " + token);
        assertFalse(normalize(text.intentOutro()).contains(normalizedToken),
                adviceId + " outro should not include " + token);
        assertFalse(normalize(text.hintA()).contains(normalizedToken),
                adviceId + " hint_a should not include " + token);
        assertFalse(normalize(text.hintB()).contains(normalizedToken),
                adviceId + " hint_b should not include " + token);
        assertFalse(normalize(text.checklist()).contains(normalizedToken),
                adviceId + " checklist should not include " + token);
        assertFalse(normalize(text.antiPatterns()).contains(normalizedToken),
                adviceId + " anti_patterns should not include " + token);
        assertFalse(normalize(text.verbose()).contains(normalizedToken),
                adviceId + " verbose should not include " + token);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
