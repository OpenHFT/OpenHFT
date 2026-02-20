/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * User-facing advice identifiers (stable; only add, never rename).
 */
public enum AdviceId {
    MMAssertionMessageMissing(RuleId.MISSING_MESSAGE, AdviceSource.ASSERTION),
    MMThrowMessageMissing(RuleId.MISSING_MESSAGE, AdviceSource.THROW),
    MMLogMessageMissing(RuleId.MISSING_MESSAGE, AdviceSource.LOG),
    MMCommentMessageMissing(RuleId.MISSING_MESSAGE, AdviceSource.COMMENT),
    MMAnnotationDisabledMessageMissing(RuleId.MISSING_MESSAGE, AdviceSource.ANNOTATION_DISABLED),

    MMCommentMapStringObject(RuleId.MAP_STRING_OBJECT, AdviceSource.COMMENT),

    MMThrowNull(RuleId.THROW_NULL, AdviceSource.THROW),
    MMAssertionAssertJOverrideMessage(RuleId.ASSERTJ_OVERRIDE, AdviceSource.ASSERTION),
    MMAssertionAssertAllHeadingLowSignal(RuleId.ASSERTALL_HEADING, AdviceSource.ASSERTION),
    MMAssertionMessageMissingLoopIndex(RuleId.MISSING_LOOP_INDEX, AdviceSource.ASSERTION),

    MMAssertionMessageRestatesDerivedAssertion(RuleId.RESTATES_DERIVED, AdviceSource.ASSERTION),
    MMPreconditionMessageRestatesDerivedAssertion(RuleId.RESTATES_DERIVED, AdviceSource.PRECONDITION),
    MMThrowMessageRestatesDerivedAssertion(RuleId.RESTATES_DERIVED, AdviceSource.THROW),
    MMAnnotationDisplayNameMessageRestatesDerivedAssertion(RuleId.RESTATES_DERIVED, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageRestatesDerivedAssertion(RuleId.RESTATES_DERIVED, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageContextless(RuleId.CONTEXTLESS, AdviceSource.ASSERTION),
    MMPreconditionMessageContextless(RuleId.CONTEXTLESS, AdviceSource.PRECONDITION),
    MMThrowMessageContextless(RuleId.CONTEXTLESS, AdviceSource.THROW),
    MMLogMessageContextless(RuleId.CONTEXTLESS, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageContextless(RuleId.CONTEXTLESS, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageContextless(RuleId.CONTEXTLESS, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.ASSERTION),
    MMPreconditionMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.PRECONDITION),
    MMThrowMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.THROW),
    MMLogMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageIndexOnly(RuleId.INDEX_ONLY, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageLongWord(RuleId.LONG_WORD, AdviceSource.ASSERTION),
    MMPreconditionMessageLongWord(RuleId.LONG_WORD, AdviceSource.PRECONDITION),
    MMThrowMessageLongWord(RuleId.LONG_WORD, AdviceSource.THROW),
    MMLogMessageLongWord(RuleId.LONG_WORD, AdviceSource.LOG),
    MMJavadocClassMessageLongWord(RuleId.LONG_WORD, AdviceSource.JAVADOC_CLASS),
    MMJavadocMemberMessageLongWord(RuleId.LONG_WORD, AdviceSource.JAVADOC_MEMBER),
    MMAnnotationDisplayNameMessageLongWord(RuleId.LONG_WORD, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageLongWord(RuleId.LONG_WORD, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageTooLong(RuleId.TOO_LONG, AdviceSource.ASSERTION),
    MMPreconditionMessageTooLong(RuleId.TOO_LONG, AdviceSource.PRECONDITION),
    MMThrowMessageTooLong(RuleId.TOO_LONG, AdviceSource.THROW),
    MMLogMessageTooLong(RuleId.TOO_LONG, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageTooLong(RuleId.TOO_LONG, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageTooLong(RuleId.TOO_LONG, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageTrivialSupplier(RuleId.TRIVIAL_SUPPLIER, AdviceSource.ASSERTION),
    MMPreconditionMessageTrivialSupplier(RuleId.TRIVIAL_SUPPLIER, AdviceSource.PRECONDITION),
    MMLogMessageTrivialSupplier(RuleId.TRIVIAL_SUPPLIER, AdviceSource.LOG),

    MMAssertionMessageGeneric(RuleId.GENERIC, AdviceSource.ASSERTION),
    MMPreconditionMessageGeneric(RuleId.GENERIC, AdviceSource.PRECONDITION),
    MMThrowMessageGeneric(RuleId.GENERIC, AdviceSource.THROW),
    MMLogMessageGeneric(RuleId.GENERIC, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageGeneric(RuleId.GENERIC, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageGeneric(RuleId.GENERIC, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageRedundantClassName(RuleId.REDUNDANT_CLASS, AdviceSource.ASSERTION),
    MMJavadocClassMessageRedundantClassName(RuleId.REDUNDANT_CLASS, AdviceSource.JAVADOC_CLASS),

    MMAssertionMessageRestatesAssertion(RuleId.RESTATES_ASSERTION, AdviceSource.ASSERTION),
    MMPreconditionMessageRestatesAssertion(RuleId.RESTATES_ASSERTION, AdviceSource.PRECONDITION),
    MMThrowMessageRestatesAssertion(RuleId.RESTATES_ASSERTION, AdviceSource.THROW),
    MMAnnotationDisplayNameMessageRestatesAssertion(RuleId.RESTATES_ASSERTION, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageRestatesAssertion(RuleId.RESTATES_ASSERTION, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.ASSERTION),
    MMPreconditionMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.PRECONDITION),
    MMThrowMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.THROW),
    MMLogMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageWhitespaceRun(RuleId.WHITESPACE_RUN, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageMissingStringSearchValue(RuleId.MISSING_STRING_VALUE, AdviceSource.ASSERTION),

    MMAssertionMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.ASSERTION),
    MMPreconditionMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.PRECONDITION),
    MMThrowMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.THROW),
    MMLogMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.LOG),
    MMAnnotationDisplayNameMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageRedundantLineNumber(RuleId.REDUNDANT_LINE, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageDuplicatesInput(RuleId.DUPLICATES_INPUT, AdviceSource.ASSERTION),

    MMAssertionMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.ASSERTION),
    MMPreconditionMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.PRECONDITION),
    MMThrowMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.THROW),
    MMLogMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.LOG),
    MMCommentMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.COMMENT),
    MMJavadocClassMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.JAVADOC_CLASS),
    MMJavadocMemberMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.JAVADOC_MEMBER),
    MMAnnotationDisplayNameMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageTooFewMeaningfulWords(RuleId.TOO_FEW_MEANINGFUL, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageMissingComparisonValues(RuleId.MISSING_COMPARISON_VALUES, AdviceSource.ASSERTION),

    MMAssertionMessageRedundantMethodName(RuleId.REDUNDANT_METHOD, AdviceSource.ASSERTION),
    MMJavadocMemberMessageRedundantMethodName(RuleId.REDUNDANT_METHOD, AdviceSource.JAVADOC_MEMBER),

    MMAssertionMessageMissingSubject(RuleId.MISSING_SUBJECT, AdviceSource.ASSERTION),
    MMLogMessageMissingSubject(RuleId.MISSING_SUBJECT, AdviceSource.LOG),
    MMCommentMessageMissingSubject(RuleId.MISSING_SUBJECT, AdviceSource.COMMENT),

    MMAssertionMessageDuplicate(RuleId.DUPLICATE, AdviceSource.ASSERTION),
    MMPreconditionMessageDuplicate(RuleId.DUPLICATE, AdviceSource.PRECONDITION),
    MMThrowMessageDuplicate(RuleId.DUPLICATE, AdviceSource.THROW),
    MMLogMessageDuplicate(RuleId.DUPLICATE, AdviceSource.LOG),
    MMCommentMessageDuplicate(RuleId.DUPLICATE, AdviceSource.COMMENT),
    MMJavadocClassMessageDuplicate(RuleId.DUPLICATE, AdviceSource.JAVADOC_CLASS),
    MMJavadocMemberMessageDuplicate(RuleId.DUPLICATE, AdviceSource.JAVADOC_MEMBER),
    MMAnnotationDisplayNameMessageDuplicate(RuleId.DUPLICATE, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageDuplicate(RuleId.DUPLICATE, AdviceSource.ANNOTATION_DISABLED),

    MMAssertionMessageTooShort(RuleId.TOO_SHORT, AdviceSource.ASSERTION),
    MMPreconditionMessageTooShort(RuleId.TOO_SHORT, AdviceSource.PRECONDITION),
    MMThrowMessageTooShort(RuleId.TOO_SHORT, AdviceSource.THROW),
    MMLogMessageTooShort(RuleId.TOO_SHORT, AdviceSource.LOG),
    MMCommentMessageTooShort(RuleId.TOO_SHORT, AdviceSource.COMMENT),
    MMJavadocClassMessageTooShort(RuleId.TOO_SHORT, AdviceSource.JAVADOC_CLASS),
    MMJavadocMemberMessageTooShort(RuleId.TOO_SHORT, AdviceSource.JAVADOC_MEMBER),
    MMAnnotationDisplayNameMessageTooShort(RuleId.TOO_SHORT, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationDisabledMessageTooShort(RuleId.TOO_SHORT, AdviceSource.ANNOTATION_DISABLED),

    MMOverusedWord(RuleId.OVERUSED_WORD, null),
    MMLacksPurpose(RuleId.LACKS_PURPOSE, null),
    MMLowEntropy(RuleId.LOW_ENTROPY, null),

    MMAnnotationDisplayNameMissing(RuleId.MISSING_DISPLAY_NAME, AdviceSource.ANNOTATION_DISPLAY_NAME),
    MMAnnotationTestOrder(RuleId.TEST_ANNOTATION_ORDER, AdviceSource.ANNOTATION_TEST_ORDER),
    MMAnnotationJUnit4Annotation(RuleId.JUNIT4_ANNOTATION, AdviceSource.ANNOTATION_JUNIT4),
    MMAnnotationJUnit4Assertion(RuleId.JUNIT4_ASSERTION, AdviceSource.ANNOTATION_JUNIT4),

    MMUnhandled(RuleId.UNHANDLED, null),

    /**
     * Placeholder for internal error handling; should not appear in output.
     */
    UNKNOWN(null, null);

    private static final Map<RuleId, Map<AdviceSource, AdviceId>> BY_RULE_AND_SOURCE;
    private static final Map<RuleId, AdviceId> BY_RULE;
    private static final Map<String, AdviceId> BY_NAME;

    static {
        Map<RuleId, Map<AdviceSource, AdviceId>> byRuleAndSource = new EnumMap<>(RuleId.class);
        Map<RuleId, AdviceId> byRule = new EnumMap<>(RuleId.class);
        Map<String, AdviceId> byName = new HashMap<>();
        for (AdviceId adviceId : values()) {
            byName.put(adviceId.name(), adviceId);
            if (adviceId.ruleId == null || adviceId == UNKNOWN) {
                continue;
            }
            if (adviceId.adviceSource == null) {
                byRule.put(adviceId.ruleId, adviceId);
            } else {
                byRuleAndSource
                        .computeIfAbsent(adviceId.ruleId, key -> new EnumMap<>(AdviceSource.class))
                        .put(adviceId.adviceSource, adviceId);
            }
        }
        BY_RULE_AND_SOURCE = Collections.unmodifiableMap(byRuleAndSource);
        BY_RULE = Collections.unmodifiableMap(byRule);
        BY_NAME = Collections.unmodifiableMap(byName);
    }

    private final RuleId ruleId;
    private final AdviceSource adviceSource;

    AdviceId(RuleId ruleId, AdviceSource adviceSource) {
        this.ruleId = ruleId;
        this.adviceSource = adviceSource;
    }

    public RuleId ruleId() {
        return ruleId;
    }

    public AdviceSource adviceSource() {
        return adviceSource;
    }

    public boolean isFileLevel() {
        return ruleId == RuleId.OVERUSED_WORD
                || ruleId == RuleId.LACKS_PURPOSE
                || ruleId == RuleId.LOW_ENTROPY;
    }

    /**
     * Look up an advice identifier for the given rule and source.
     *
     * @param ruleId rule identifier.
     * @param adviceSource advice source.
     * @return matching advice identifier, or {@link #UNKNOWN}.
     */
    public static AdviceId forRule(RuleId ruleId, AdviceSource adviceSource) {
        if (ruleId == null || adviceSource == null) {
            return UNKNOWN;
        }
        Map<AdviceSource, AdviceId> bySource = BY_RULE_AND_SOURCE.get(ruleId);
        if (bySource == null) {
            return UNKNOWN;
        }
        AdviceId adviceId = bySource.get(adviceSource);
        return adviceId == null ? UNKNOWN : adviceId;
    }

    /**
     * Look up a file-level advice identifier for the given rule.
     *
     * @param ruleId rule identifier.
     * @return matching advice identifier, or {@link #UNKNOWN}.
     */
    public static AdviceId forRule(RuleId ruleId) {
        if (ruleId == null) {
            return UNKNOWN;
        }
        AdviceId adviceId = BY_RULE.get(ruleId);
        return adviceId == null ? UNKNOWN : adviceId;
    }

    /**
     * Look up an advice identifier by its enum name.
     *
     * @param name advice identifier name.
     * @return matching advice identifier, or {@link #UNKNOWN}.
     */
    public static AdviceId forName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }
        AdviceId adviceId = BY_NAME.get(name);
        return adviceId == null ? UNKNOWN : adviceId;
    }
}
