/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.EnumSet;

/**
 * Identifies message rules with message keys, codes, and priority ordering.
 */
public enum RuleId {
    /**
     * Missing message for a candidate.
     */
    MISSING_MESSAGE("assert.message.missing.message", "MMMissingMessage", -1,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.THROW,
                    MessageSource.ANNOTATION, MessageSource.LOG, MessageSource.COMMENT)),
    /**
     * Map<String, Object> or Map<String, ?> used without a reason comment.
     */
    MAP_STRING_OBJECT("assert.message.map.string.object", "MMMapStringObject", 30,
            EnumSet.of(MessageSource.COMMENT)),
    /**
     * Throw statement uses a null literal.
     */
    THROW_NULL("assert.message.throw.null", "MMThrowNull", -2,
            EnumSet.of(MessageSource.THROW)),
    /**
     * Generic AssertJ override message.
     */
    ASSERTJ_OVERRIDE("assert.message.assertj.override", "MMAssertJGenericOverride", 0,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Low-signal assertAll heading.
     */
    ASSERTALL_HEADING("assert.message.assertall.heading", "MMLowSignalAssertAllHeading", 0,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Missing loop index in the message.
     */
    MISSING_LOOP_INDEX("assert.message.missing.loop.index", "MMMissingLoopIndex", 0,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Restates derived assertion wording.
     */
    RESTATES_DERIVED("assert.message.restates.derived", "MMRestatesDerivedAssertion", 0,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION)),
    /**
     * Contextless message with little information.
     */
    CONTEXTLESS("assert.message.contextless", "MMContextless", 1,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Message contains only an index.
     */
    INDEX_ONLY("assert.message.index.only", "MMIndexOnly", 1,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Message contains an overly long word.
     */
    LONG_WORD("assert.message.long.word", "MMLongWord", 1,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG,
                    MessageSource.JAVADOC_CLASS, MessageSource.JAVADOC_MEMBER)),
    /**
     * Message is too long.
     */
    TOO_LONG("assert.message.too.long", "MMTooLong", 10,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Trivial supplier message.
     */
    TRIVIAL_SUPPLIER("assert.message.trivial.supplier", "MMTrivialSupplier", 11,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION, MessageSource.LOG)),
    /**
     * Generic message with low signal.
     */
    GENERIC("assert.message.generic", "MMGenericMessage", 12,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Redundant class name in message.
     */
    REDUNDANT_CLASS("assert.message.redundant.class", "MMRedundantClassName", 13,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.JAVADOC_CLASS)),
    /**
     * Restates the assertion wording.
     */
    RESTATES_ASSERTION("assert.message.restates.assertion", "MMRestatesAssertion", 14,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION)),
    /**
     * Contains a run of repeated whitespace.
     */
    WHITESPACE_RUN("assert.message.whitespace.run", "MMWhitespaceRun", 15,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Missing string search value.
     */
    MISSING_STRING_VALUE("assert.message.missing.string.value", "MMMissingStringSearchValue", 16,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Redundant line number in message.
     */
    REDUNDANT_LINE("assert.message.redundant.line", "MMRedundantLineNumber", 17,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG)),
    /**
     * Message duplicates an input value.
     */
    DUPLICATES_INPUT("assert.message.duplicates.input", "MMDuplicatesInput", 18,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Too few meaningful words.
     */
    TOO_FEW_MEANINGFUL("assert.message.too.few.meaningful", "MMTooFewMeaningfulWords", 19,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG,
                    MessageSource.COMMENT, MessageSource.JAVADOC_CLASS, MessageSource.JAVADOC_MEMBER)),
    /**
     * Missing comparison values in the message.
     */
    MISSING_COMPARISON_VALUES("assert.message.missing.comparison.values", "MMMissingComparisonValues",
            20, EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Redundant method name in message.
     */
    REDUNDANT_METHOD("assert.message.redundant.method", "MMRedundantMethodName", 21,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.JAVADOC_MEMBER)),
    /**
     * Missing subject in message text.
     */
    MISSING_SUBJECT("assert.message.missing.subject", "MMMissingSubject", 22,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.LOG, MessageSource.COMMENT)),
    /**
     * Duplicate message within the same file.
     */
    DUPLICATE("assert.message.duplicate", "MMDuplicate", 23,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG,
                    MessageSource.COMMENT, MessageSource.JAVADOC_CLASS, MessageSource.JAVADOC_MEMBER)),
    /**
     * Message is too short.
     */
    TOO_SHORT("assert.message.too.short", "MMTooShort", 24,
            EnumSet.of(MessageSource.ASSERTION, MessageSource.PRECONDITION,
                    MessageSource.THROW, MessageSource.ANNOTATION, MessageSource.LOG,
                    MessageSource.COMMENT, MessageSource.JAVADOC_CLASS, MessageSource.JAVADOC_MEMBER)),
    /**
     * Overused meaningful word across a file.
     */
    OVERUSED_WORD("assert.message.overused.word", "MMOverusedWord", 27,
            EnumSet.allOf(MessageSource.class)),
    /**
     * Not enough rationale cues across a file.
     */
    LACKS_PURPOSE("assert.message.lacks.purpose", "MMLacksPurpose", 28,
            EnumSet.allOf(MessageSource.class)),
    /**
     * Low message entropy across a file.
     */
    LOW_ENTROPY("assert.message.low.entropy", "MMLowEntropy", 29,
            EnumSet.allOf(MessageSource.class)),
    /**
     * Missing @DisplayName annotation on JUnit 5 test method.
     */
    MISSING_DISPLAY_NAME("assert.message.missing.display.name", "MMDisplayName", 25,
            EnumSet.of(MessageSource.ANNOTATION)),
    /**
     * @Test annotation should appear first on test methods.
     */
    TEST_ANNOTATION_ORDER("assert.message.test.annotation.order", "MMTestAnnotationOrder", 26,
            EnumSet.of(MessageSource.ANNOTATION)),
    /**
     * JUnit 4 test annotations used in a file.
     */
    JUNIT4_ANNOTATION("assert.message.junit4.annotation", "MMJUnit4Annotation", 90,
            EnumSet.of(MessageSource.ANNOTATION)),
    /**
     * JUnit 4 assertions used in a file.
     */
    JUNIT4_ASSERTION("assert.message.junit4.assertion", "MMJUnit4Assertion", 91,
            EnumSet.of(MessageSource.ASSERTION)),
    /**
     * Unhandled extraction case, reported unless disabled.
     */
    UNHANDLED("assert.message.unhandled", "MMUnhandled", 1000,
            EnumSet.allOf(MessageSource.class));

    private static final String INTENT_SUFFIX = ".intent";
    private final String messageKey;
    private final String code;
    private final int priority;
    private final EnumSet<MessageSource> sources;

    RuleId(String messageKey, String code, int priority, EnumSet<MessageSource> sources) {
        this.messageKey = messageKey;
        this.code = code;
        this.priority = priority;
        this.sources = sources;
    }

    /**
     * Return the verbose message key used for Checkstyle output.
     *
     * @return verbose message key.
     */
    public String messageKey() {
        return messageKey;
    }

    /**
     * Return the intent-first message key used for concise output.
     *
     * @return intent message key.
     */
    public String intentMessageKey() {
        return messageKey + INTENT_SUFFIX;
    }

    /**
     * Return the message key for the chosen verbosity.
     *
     * @param verbose {@code true} for verbose output.
     * @return message key for the selected verbosity.
     */
    public String messageKey(boolean verbose) {
        return verbose ? messageKey : intentMessageKey();
    }

    /**
     * Return the message key for the chosen verbosity.
     *
     * @param verboseKey verbose key.
     * @param verbose    {@code true} for verbose output.
     * @return message key for the selected verbosity.
     */
    public static String messageKey(String verboseKey, boolean verbose) {
        return verbose ? verboseKey : verboseKey + INTENT_SUFFIX;
    }

    /**
     * Return the short rule code.
     *
     * @return short rule code.
     */
    public String code() {
        return code;
    }

    /**
     * Return the rule priority, with lower values evaluated first.
     *
     * @return rule priority.
     */
    public int priority() {
        return priority;
    }

    /**
     * Return the enum order for tie-breaking.
     *
     * @return ordinal order.
     */
    public int order() {
        return ordinal();
    }

    /**
     * Check whether the rule applies to the given source.
     *
     * @param source message source to test.
     * @return {@code true} if the rule applies.
     */
    public boolean appliesTo(MessageSource source) {
        return source != null && sources.contains(source);
    }
}
