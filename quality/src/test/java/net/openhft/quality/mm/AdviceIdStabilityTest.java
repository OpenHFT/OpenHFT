/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the {@link AdviceId} enum against accidental renames or removals.
 * <p>
 * New entries may be appended freely — update the golden list when you do.
 * Renaming or removing an existing entry is a breaking change that invalidates
 * persisted JSONL output and suppression comments.
 */
@DisplayName("AdviceId stability golden-list tests")
class AdviceIdStabilityTest {

    /** Canonical golden list — append only, never reorder or remove. */
    private static final List<String> GOLDEN = List.of(
            "MMAssertionMessageMissing",
            "MMThrowMessageMissing",
            "MMLogMessageMissing",
            "MMCommentMessageMissing",
            "MMAnnotationDisabledMessageMissing",
            "MMCommentMapStringObject",
            "MMThrowNull",
            "MMAssertionAssertJOverrideMessage",
            "MMAssertionAssertAllHeadingLowSignal",
            "MMAssertionMessageMissingLoopIndex",
            "MMAssertionMessageRestatesDerivedAssertion",
            "MMPreconditionMessageRestatesDerivedAssertion",
            "MMThrowMessageRestatesDerivedAssertion",
            "MMAnnotationDisplayNameMessageRestatesDerivedAssertion",
            "MMAnnotationDisabledMessageRestatesDerivedAssertion",
            "MMAssertionMessageContextless",
            "MMPreconditionMessageContextless",
            "MMThrowMessageContextless",
            "MMLogMessageContextless",
            "MMAnnotationDisplayNameMessageContextless",
            "MMAnnotationDisabledMessageContextless",
            "MMAssertionMessageIndexOnly",
            "MMPreconditionMessageIndexOnly",
            "MMThrowMessageIndexOnly",
            "MMLogMessageIndexOnly",
            "MMAnnotationDisplayNameMessageIndexOnly",
            "MMAnnotationDisabledMessageIndexOnly",
            "MMAssertionMessageLongWord",
            "MMPreconditionMessageLongWord",
            "MMThrowMessageLongWord",
            "MMLogMessageLongWord",
            "MMJavadocClassMessageLongWord",
            "MMJavadocMemberMessageLongWord",
            "MMAnnotationDisplayNameMessageLongWord",
            "MMAnnotationDisabledMessageLongWord",
            "MMAssertionMessageTooLong",
            "MMPreconditionMessageTooLong",
            "MMThrowMessageTooLong",
            "MMLogMessageTooLong",
            "MMAnnotationDisplayNameMessageTooLong",
            "MMAnnotationDisabledMessageTooLong",
            "MMAssertionMessageTrivialSupplier",
            "MMPreconditionMessageTrivialSupplier",
            "MMLogMessageTrivialSupplier",
            "MMAssertionMessageGeneric",
            "MMPreconditionMessageGeneric",
            "MMThrowMessageGeneric",
            "MMLogMessageGeneric",
            "MMAnnotationDisplayNameMessageGeneric",
            "MMAnnotationDisabledMessageGeneric",
            "MMAssertionMessageRedundantClassName",
            "MMJavadocClassMessageRedundantClassName",
            "MMAssertionMessageRestatesAssertion",
            "MMPreconditionMessageRestatesAssertion",
            "MMThrowMessageRestatesAssertion",
            "MMAnnotationDisplayNameMessageRestatesAssertion",
            "MMAnnotationDisabledMessageRestatesAssertion",
            "MMAssertionMessageWhitespaceRun",
            "MMPreconditionMessageWhitespaceRun",
            "MMThrowMessageWhitespaceRun",
            "MMLogMessageWhitespaceRun",
            "MMAnnotationDisplayNameMessageWhitespaceRun",
            "MMAnnotationDisabledMessageWhitespaceRun",
            "MMAssertionMessageMissingStringSearchValue",
            "MMAssertionMessageRedundantLineNumber",
            "MMPreconditionMessageRedundantLineNumber",
            "MMThrowMessageRedundantLineNumber",
            "MMLogMessageRedundantLineNumber",
            "MMAnnotationDisplayNameMessageRedundantLineNumber",
            "MMAnnotationDisabledMessageRedundantLineNumber",
            "MMAssertionMessageDuplicatesInput",
            "MMAssertionMessageTooFewMeaningfulWords",
            "MMPreconditionMessageTooFewMeaningfulWords",
            "MMThrowMessageTooFewMeaningfulWords",
            "MMLogMessageTooFewMeaningfulWords",
            "MMCommentMessageTooFewMeaningfulWords",
            "MMJavadocClassMessageTooFewMeaningfulWords",
            "MMJavadocMemberMessageTooFewMeaningfulWords",
            "MMAnnotationDisplayNameMessageTooFewMeaningfulWords",
            "MMAnnotationDisabledMessageTooFewMeaningfulWords",
            "MMAssertionMessageMissingComparisonValues",
            "MMAssertionMessageRedundantMethodName",
            "MMJavadocMemberMessageRedundantMethodName",
            "MMAssertionMessageMissingSubject",
            "MMLogMessageMissingSubject",
            "MMCommentMessageMissingSubject",
            "MMAssertionMessageDuplicate",
            "MMPreconditionMessageDuplicate",
            "MMThrowMessageDuplicate",
            "MMLogMessageDuplicate",
            "MMCommentMessageDuplicate",
            "MMJavadocClassMessageDuplicate",
            "MMJavadocMemberMessageDuplicate",
            "MMAnnotationDisplayNameMessageDuplicate",
            "MMAnnotationDisabledMessageDuplicate",
            "MMAssertionMessageTooShort",
            "MMPreconditionMessageTooShort",
            "MMThrowMessageTooShort",
            "MMLogMessageTooShort",
            "MMCommentMessageTooShort",
            "MMJavadocClassMessageTooShort",
            "MMJavadocMemberMessageTooShort",
            "MMAnnotationDisplayNameMessageTooShort",
            "MMAnnotationDisabledMessageTooShort",
            "MMOverusedWord",
            "MMLacksPurpose",
            "MMLowEntropy",
            "MMAnnotationDisplayNameMissing",
            "MMAnnotationTestOrder",
            "MMAnnotationJUnit4Annotation",
            "MMAnnotationJUnit4Assertion",
            "MMUnhandled",
            "UNKNOWN"
    );

    @Test
    @DisplayName("AdviceId enum values match golden list exactly")
    void adviceIdValuesMatchGoldenList() {
        List<String> actual = Arrays.stream(AdviceId.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(GOLDEN, actual,
                "AdviceId enum values diverged from golden list — "
                        + "if you added a new entry, append it to GOLDEN in AdviceIdStabilityTest; "
                        + "if you renamed or removed an entry, that is a breaking change");
    }
}
