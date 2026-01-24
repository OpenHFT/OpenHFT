/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AdviceId coverage for known trigger scenarios")
final class AdviceIdCoverageTest {
    private static final MessageMetricsCalculator METRICS_CALCULATOR = new MessageMetricsCalculator();
    private static final MessageRuleSupport RULE_SUPPORT = new MessageRuleSupport(METRICS_CALCULATOR);
    private static final String LONG_WORD = "supercalifragilisticexpialidocioussupercalifragilistic";
    private static final String LONG_MESSAGE = buildLongMessage();

    private static final Set<AdviceId> MANUAL_ADVICE = EnumSet.of(
            AdviceId.MMOverusedWord,
            AdviceId.MMLacksPurpose,
            AdviceId.MMLowEntropy,
            AdviceId.MMAnnotationDisplayNameMissing,
            AdviceId.MMAnnotationTestOrder,
            AdviceId.MMAnnotationJUnit4Annotation,
            AdviceId.MMAnnotationJUnit4Assertion,
            AdviceId.MMUnhandled
    );

    @Test
    @DisplayName("Each AdviceId has a trigger scenario")
    void adviceIdCoverage() {
        EnumSet<AdviceId> missing = EnumSet.noneOf(AdviceId.class);
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            if (MANUAL_ADVICE.contains(adviceId)) {
                continue;
            }
            if (adviceId.isFileLevel()) {
                continue;
            }
            RuleId ruleId = adviceId.ruleId();
            Scenario scenario = scenarioFor(ruleId);
            assertNotNull(scenario, "Missing scenario for rule " + ruleId);
            AbstractMessageRule rule = ruleFor(ruleId);
            assertNotNull(rule, "Missing rule implementation for " + ruleId);
            if (!triggerAdvice(rule, adviceId, scenario)) {
                missing.add(adviceId);
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError("Coverage missing for AdviceId(s): " + missing);
        }
    }

    private static boolean triggerAdvice(AbstractMessageRule rule, AdviceId adviceId, Scenario scenario) {
        EnumSet<AdviceId> seen = EnumSet.noneOf(AdviceId.class);
        RecordingAdviceEmitter emitter = new RecordingAdviceEmitter(seen);
        ViolationCollector collector = new ViolationCollector(null);
        if (rule.ruleId() == RuleId.DUPLICATE) {
            MessageCandidate first = buildCandidate(adviceId, scenario, 10);
            MessageCandidate second = buildCandidate(adviceId, scenario, 20);
            evaluate(rule, first, scenario, emitter, collector);
            evaluate(rule, second, scenario, emitter, collector);
        } else {
            MessageCandidate candidate = buildCandidate(adviceId, scenario, 10);
            evaluate(rule, candidate, scenario, emitter, collector);
        }
        return seen.contains(adviceId);
    }

    private static void evaluate(AbstractMessageRule rule, MessageCandidate candidate, Scenario scenario,
                                 AdviceEmitter emitter, ViolationCollector collector) {
        MessageMetrics metrics = candidate.message() == null
                ? null
                : METRICS_CALCULATOR.calculate(candidate.message(), candidate.placeholderCount(),
                candidate.keyValueLabelCount());
        MessageContext context = new MessageContext(candidate, metrics,
                scenario.className, scenario.methodName, false,
                RULE_SUPPORT, new SuppressionTracker(), emitter);
        rule.evaluate(context, collector, new RuleEvaluationState());
    }

    private static MessageCandidate buildCandidate(AdviceId adviceId, Scenario scenario, int lineNo) {
        AdviceSource adviceSource = adviceId.adviceSource();
        MessageSource source = messageSourceFor(adviceSource);
        MessageCandidate.Builder builder = new MessageCandidate.Builder()
                .source(source)
                .adviceSource(adviceSource)
                .lineNo(lineNo);
        if (scenario.message != null) {
            builder.message(scenario.message)
                    .messageExpr(scenario.messageExpr)
                    .normalisedMessage(MessageNormaliser.normalise(scenario.message))
                    .placeholderCount(0)
                    .keyValueLabelCount(0)
                    .constantMessage(scenario.constantMessage);
        }
        if (scenario.missingMessage) {
            builder.missingMessage(true).missingMessageKind(scenario.missingMessageKind);
        }
        if (scenario.throwNull) {
            builder.throwNull(true);
        }
        if (scenario.assertAllHeading) {
            builder.assertAllHeading(true);
        }
        if (scenario.assertJOverride) {
            builder.assertJOverride(true);
        }
        if (scenario.trivialSupplierDescription != null) {
            builder.trivialSupplierDescription(scenario.trivialSupplierDescription);
        }
        if (scenario.inputValues != null) {
            builder.inputValues(scenario.inputValues);
        }
        if (scenario.loopNames != null) {
            builder.loopNames(scenario.loopNames);
        }
        if (scenario.missingLoopIndex) {
            builder.missingLoopIndex(true);
        }
        if (scenario.comparisonOperator != null) {
            builder.comparisonOperator(scenario.comparisonOperator)
                    .comparisonLeftOperand(scenario.comparisonLeftOperand)
                    .comparisonRightOperand(scenario.comparisonRightOperand);
        }
        if (scenario.stringSearchMethod != null) {
            builder.stringSearchMethod(scenario.stringSearchMethod)
                    .stringSearchTarget(scenario.stringSearchTarget)
                    .stringSearchArg(scenario.stringSearchArg);
        }
        return builder.build();
    }

    private static MessageSource messageSourceFor(AdviceSource adviceSource) {
        if (adviceSource == null) {
            return null;
        }
        switch (adviceSource) {
            case ASSERTION:
                return MessageSource.ASSERTION;
            case PRECONDITION:
                return MessageSource.PRECONDITION;
            case THROW:
                return MessageSource.THROW;
            case LOG:
                return MessageSource.LOG;
            case COMMENT:
                return MessageSource.COMMENT;
            case JAVADOC_CLASS:
                return MessageSource.JAVADOC_CLASS;
            case JAVADOC_MEMBER:
                return MessageSource.JAVADOC_MEMBER;
            case ANNOTATION_DISPLAY_NAME:
            case ANNOTATION_DISABLED:
            case ANNOTATION_TEST_ORDER:
            case ANNOTATION_JUNIT4:
                return MessageSource.ANNOTATION;
            default:
                throw new IllegalStateException("Unhandled advice source: " + adviceSource);
        }
    }

    private static AbstractMessageRule ruleFor(RuleId ruleId) {
        switch (ruleId) {
            case MISSING_MESSAGE:
                return new MMMissingMessage();
            case THROW_NULL:
                return new MMThrowNull();
            case ASSERTJ_OVERRIDE:
                return new MMAssertJGenericOverride();
            case ASSERTALL_HEADING:
                return new MMLowSignalAssertAllHeading();
            case MISSING_LOOP_INDEX:
                return new MMMissingLoopIndex();
            case RESTATES_DERIVED:
                return new MMRestatesDerivedAssertion();
            case CONTEXTLESS:
                return new MMContextless();
            case INDEX_ONLY:
                return new MMIndexOnly();
            case LONG_WORD:
                return new MMLongWord();
            case TOO_LONG:
                return new MMTooLong();
            case TRIVIAL_SUPPLIER:
                return new MMTrivialSupplier();
            case GENERIC:
                return new MMGenericMessage();
            case REDUNDANT_CLASS:
                return new MMRedundantClassName();
            case RESTATES_ASSERTION:
                return new MMRestatesAssertion();
            case WHITESPACE_RUN:
                return new MMWhitespaceRun();
            case MISSING_STRING_VALUE:
                return new MMMissingStringSearchValue();
            case REDUNDANT_LINE:
                return new MMRedundantLineNumber();
            case DUPLICATES_INPUT:
                return new MMDuplicatesInput();
            case TOO_FEW_MEANINGFUL:
                return new MMTooFewMeaningfulWords();
            case MISSING_COMPARISON_VALUES:
                return new MMMissingComparisonValues();
            case REDUNDANT_METHOD:
                return new MMRedundantMethodName();
            case MISSING_SUBJECT:
                return new MMMissingSubject();
            case DUPLICATE:
                return new MMDuplicate(new java.util.HashMap<>());
            case TOO_SHORT:
                return new MMTooShort();
            default:
                return null;
        }
    }

    private static Scenario scenarioFor(RuleId ruleId) {
        if (ruleId == null) {
            return null;
        }
        switch (ruleId) {
            case MISSING_MESSAGE:
                return Scenario.missingMessage();
            case THROW_NULL:
                return Scenario.throwNull();
            case ASSERTJ_OVERRIDE:
                return Scenario.message("expected").assertJOverride();
            case ASSERTALL_HEADING:
                return Scenario.message("checks").assertAllHeading();
            case MISSING_LOOP_INDEX:
                return Scenario.message("loop index").missingLoopIndex(Arrays.asList("i"));
            case RESTATES_DERIVED:
                return Scenario.message("empty");
            case CONTEXTLESS:
                return Scenario.message("validation");
            case INDEX_ONLY:
                return Scenario.message("index 7");
            case LONG_WORD:
                return Scenario.message(LONG_WORD);
            case TOO_LONG:
                return Scenario.message(LONG_MESSAGE);
            case TRIVIAL_SUPPLIER:
                return Scenario.message("supplier").trivialSupplier("constant supplier");
            case GENERIC:
                return Scenario.message("expected");
            case REDUNDANT_CLASS:
                return Scenario.message("AdviceIdCoverageFixture failed")
                        .withClassName("AdviceIdCoverageFixture");
            case RESTATES_ASSERTION:
                return Scenario.message("should be true");
            case WHITESPACE_RUN:
                return Scenario.message("cache  entry");
            case MISSING_STRING_VALUE:
                return Scenario.message("search value missing")
                        .stringSearch("contains", "text", "\"needle\"");
            case REDUNDANT_LINE:
                return Scenario.message("line 42");
            case DUPLICATES_INPUT:
                return Scenario.message("expected value").inputValues(Collections.singletonList("value"));
            case TOO_FEW_MEANINGFUL:
                return Scenario.message("the expected value is the result");
            case MISSING_COMPARISON_VALUES:
                return Scenario.message("expected comparison")
                        .comparison("==", "left", "right");
            case REDUNDANT_METHOD:
                return Scenario.message("triggerRedundantMethod failed")
                        .withMethodName("triggerRedundantMethod");
            case MISSING_SUBJECT:
                return Scenario.message("should handle input");
            case DUPLICATE:
                return Scenario.message("duplicate message");
            case TOO_SHORT:
                return Scenario.message("cache");
            default:
                return null;
        }
    }

    private static String buildLongMessage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 43; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append("word").append(i);
        }
        return builder.toString();
    }

    private static final class RecordingAdviceEmitter implements AdviceEmitter {
        private final Set<AdviceId> seen;

        private RecordingAdviceEmitter(Set<AdviceId> seen) {
            this.seen = seen;
        }

        @Override
        public void record(MessageContext context, RuleId ruleId) {
            if (context == null || ruleId == null) {
                return;
            }
            MessageCandidate candidate = context.candidate();
            if (candidate == null) {
                return;
            }
            AdviceSource adviceSource = candidate.adviceSource();
            if (adviceSource == null) {
                adviceSource = AdviceSource.fromMessageSource(candidate.source());
            }
            if (adviceSource == null) {
                return;
            }
            AdviceId adviceId = AdviceId.forRule(ruleId, adviceSource);
            if (adviceId != AdviceId.UNKNOWN) {
                seen.add(adviceId);
            }
        }
    }

    private static final class Scenario {
        private final String message;
        private final String messageExpr;
        private final boolean constantMessage;
        private final boolean missingMessage;
        private final MissingMessageKind missingMessageKind;
        private final boolean throwNull;
        private final boolean assertAllHeading;
        private final boolean assertJOverride;
        private final String trivialSupplierDescription;
        private final List<String> inputValues;
        private final List<String> loopNames;
        private final boolean missingLoopIndex;
        private final String comparisonOperator;
        private final String comparisonLeftOperand;
        private final String comparisonRightOperand;
        private final String stringSearchMethod;
        private final String stringSearchTarget;
        private final String stringSearchArg;
        private final String className;
        private final String methodName;

        private Scenario(String message, String messageExpr, boolean constantMessage,
                         boolean missingMessage, MissingMessageKind missingMessageKind,
                         boolean throwNull, boolean assertAllHeading, boolean assertJOverride,
                         String trivialSupplierDescription, List<String> inputValues,
                         List<String> loopNames, boolean missingLoopIndex,
                         String comparisonOperator, String comparisonLeftOperand, String comparisonRightOperand,
                         String stringSearchMethod, String stringSearchTarget, String stringSearchArg,
                         String className, String methodName) {
            this.message = message;
            this.messageExpr = messageExpr;
            this.constantMessage = constantMessage;
            this.missingMessage = missingMessage;
            this.missingMessageKind = missingMessageKind;
            this.throwNull = throwNull;
            this.assertAllHeading = assertAllHeading;
            this.assertJOverride = assertJOverride;
            this.trivialSupplierDescription = trivialSupplierDescription;
            this.inputValues = inputValues;
            this.loopNames = loopNames;
            this.missingLoopIndex = missingLoopIndex;
            this.comparisonOperator = comparisonOperator;
            this.comparisonLeftOperand = comparisonLeftOperand;
            this.comparisonRightOperand = comparisonRightOperand;
            this.stringSearchMethod = stringSearchMethod;
            this.stringSearchTarget = stringSearchTarget;
            this.stringSearchArg = stringSearchArg;
            this.className = className;
            this.methodName = methodName;
        }

        private static Scenario message(String message) {
            return new Scenario(message, null, true, false, null,
                    false, false, false, null,
                    null, null, false,
                    null, null, null,
                    null, null, null, null, null);
        }

        private static Scenario missingMessage() {
            return new Scenario(null, null, false, true, null,
                    false, false, false, null,
                    null, null, false,
                    null, null, null,
                    null, null, null, null, null);
        }

        private static Scenario throwNull() {
            return new Scenario(null, null, false, false, null,
                    true, false, false, null,
                    null, null, false,
                    null, null, null,
                    null, null, null, null, null);
        }

        private Scenario assertAllHeading() {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, true, assertJOverride, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario assertJOverride() {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, true, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario trivialSupplier(String description) {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, description,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario inputValues(List<String> values) {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    values, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario missingLoopIndex(List<String> names) {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    inputValues, names, true,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario comparison(String operator, String left, String right) {
            return new Scenario(message, messageExpr, true, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    operator, left, right,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, methodName);
        }

        private Scenario stringSearch(String method, String target, String arg) {
            return new Scenario(message, messageExpr, true, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    method, target, arg, className, methodName);
        }

        private Scenario withClassName(String name) {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, name, methodName);
        }

        private Scenario withMethodName(String name) {
            return new Scenario(message, messageExpr, constantMessage, missingMessage, missingMessageKind,
                    throwNull, assertAllHeading, assertJOverride, trivialSupplierDescription,
                    inputValues, loopNames, missingLoopIndex,
                    comparisonOperator, comparisonLeftOperand, comparisonRightOperand,
                    stringSearchMethod, stringSearchTarget, stringSearchArg, className, name);
        }
    }
}
