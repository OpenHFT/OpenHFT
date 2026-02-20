/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.List;

/**
 * Immutable description of a message candidate extracted from source.
 */
public class MessageCandidate {
    private final MessageSource source;
    private final AdviceSource adviceSource;
    private final int lineNo;
    private final String message;
    private final String messageExpr;
    private final String normalisedMessage;
    private final int placeholderCount;
    private final int keyValueLabelCount;
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
    private final boolean argumentNameMessage;

    private MessageCandidate(Builder builder) {
        this.source = builder.source;
        this.adviceSource = builder.adviceSource;
        this.lineNo = builder.lineNo;
        this.message = builder.message;
        this.messageExpr = builder.messageExpr;
        this.normalisedMessage = builder.normalisedMessage;
        this.placeholderCount = builder.placeholderCount;
        this.keyValueLabelCount = builder.keyValueLabelCount;
        this.constantMessage = builder.constantMessage;
        this.missingMessage = builder.missingMessage;
        this.missingMessageKind = builder.missingMessageKind;
        this.throwNull = builder.throwNull;
        this.assertAllHeading = builder.assertAllHeading;
        this.assertJOverride = builder.assertJOverride;
        this.trivialSupplierDescription = builder.trivialSupplierDescription;
        this.inputValues = builder.inputValues == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(builder.inputValues);
        this.loopNames = builder.loopNames == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(builder.loopNames);
        this.missingLoopIndex = builder.missingLoopIndex;
        this.comparisonOperator = builder.comparisonOperator;
        this.comparisonLeftOperand = builder.comparisonLeftOperand;
        this.comparisonRightOperand = builder.comparisonRightOperand;
        this.stringSearchMethod = builder.stringSearchMethod;
        this.stringSearchTarget = builder.stringSearchTarget;
        this.stringSearchArg = builder.stringSearchArg;
        this.argumentNameMessage = builder.argumentNameMessage;
    }

    /**
     * Return the source category for this candidate.
     *
     * @return origin of the candidate (assertion, log, throw, or annotation).
     */
    public MessageSource source() {
        return source;
    }

    /**
     * Return the advice source override for this candidate, if specified.
     *
     * @return advice source override, or {@code null}.
     */
    public AdviceSource adviceSource() {
        return adviceSource;
    }

    /**
     * Return the line number for the candidate.
     *
     * @return line number for the candidate.
     */
    public int lineNo() {
        return lineNo;
    }

    /**
     * Return the raw message text for the candidate.
     *
     * @return message text, or {@code null} when missing.
     */
    public String message() {
        return message;
    }

    /**
     * Return the original message expression text, if available.
     *
     * @return message expression text, or {@code null}.
     */
    public String messageExpr() {
        return messageExpr;
    }

    /**
     * Return the normalised message text used for comparisons.
     *
     * @return normalised message text used for comparisons.
     */
    public String normalisedMessage() {
        return normalisedMessage;
    }

    /**
     * Return the number of placeholders identified in the message.
     *
     * @return number of placeholders identified in the message.
     */
    public int placeholderCount() {
        return placeholderCount;
    }

    /**
     * Return the number of key-value labels identified in the message.
     *
     * @return number of key-value labels identified in the message.
     */
    public int keyValueLabelCount() {
        return keyValueLabelCount;
    }

    /**
     * Return whether the message is a compile-time constant.
     *
     * @return {@code true} if the message is a compile-time constant.
     */
    public boolean constantMessage() {
        return constantMessage;
    }

    /**
     * Return whether a message is missing for this source.
     *
     * @return {@code true} if a message is missing for this source.
     */
    public boolean missingMessage() {
        return missingMessage;
    }

    /**
     * Return the missing message kind for fix guidance.
     *
     * @return missing message kind, or {@code null} when unspecified.
     */
    public MissingMessageKind missingMessageKind() {
        return missingMessageKind;
    }

    /**
     * Return whether the throw statement uses a null literal.
     *
     * @return {@code true} if the throw statement is a null literal.
     */
    public boolean throwNull() {
        return throwNull;
    }

    /**
     * Return whether this candidate represents an assertAll heading.
     *
     * @return {@code true} if this candidate represents an assertAll heading.
     */
    public boolean assertAllHeading() {
        return assertAllHeading;
    }

    /**
     * Return whether the message was supplied via an AssertJ override.
     *
     * @return {@code true} if the message was supplied via an AssertJ override.
     */
    public boolean assertJOverride() {
        return assertJOverride;
    }

    /**
     * Return the trivial supplier description, if detected.
     *
     * @return trivial supplier description if detected, otherwise {@code null}.
     */
    public String trivialSupplierDescription() {
        return trivialSupplierDescription;
    }

    /**
     * Return input values used in the message context.
     *
     * @return input values used in the message context.
     */
    public List<String> inputValues() {
        return inputValues;
    }

    /**
     * Return loop variable names for the surrounding loop, if available.
     *
     * @return loop variable names for the surrounding loop, if available.
     */
    public List<String> loopNames() {
        return loopNames;
    }

    /**
     * Return whether a loop index is required but missing from the message.
     *
     * @return {@code true} if a loop index is required but missing from the message.
     */
    public boolean missingLoopIndex() {
        return missingLoopIndex;
    }

    /**
     * Return the comparison operator inferred from a boolean assertion.
     *
     * @return comparison operator inferred from a boolean assertion, if any.
     */
    public String comparisonOperator() {
        return comparisonOperator;
    }

    /**
     * Return the left operand name for a comparison.
     *
     * @return left operand name for a comparison, or {@code null}.
     */
    public String comparisonLeftOperand() {
        return comparisonLeftOperand;
    }

    /**
     * Return the right operand name for a comparison.
     *
     * @return right operand name for a comparison, or {@code null}.
     */
    public String comparisonRightOperand() {
        return comparisonRightOperand;
    }

    /**
     * Return the string search method name.
     *
     * @return string search method name, or {@code null}.
     */
    public String stringSearchMethod() {
        return stringSearchMethod;
    }

    /**
     * Return the string search target variable.
     *
     * @return string search target variable, or {@code null}.
     */
    public String stringSearchTarget() {
        return stringSearchTarget;
    }

    /**
     * Return the string search argument.
     *
     * @return string search argument, or {@code null}.
     */
    public String stringSearchArg() {
        return stringSearchArg;
    }

    /**
     * Return whether the message mirrors the first argument for requireNonNull/requireNotNull.
     *
     * @return {@code true} when the message matches the first argument text.
     */
    public boolean argumentNameMessage() {
        return argumentNameMessage;
    }

    /**
     * Fluent builder for {@link MessageCandidate}.
     */
    public static final class Builder {
        private MessageSource source;
        private AdviceSource adviceSource;
        private int lineNo;
        private String message;
        private String messageExpr;
        private String normalisedMessage;
        private int placeholderCount;
        private int keyValueLabelCount;
        private boolean constantMessage;
        private boolean missingMessage;
        private MissingMessageKind missingMessageKind;
        private boolean throwNull;
        private boolean assertAllHeading;
        private boolean assertJOverride;
        private String trivialSupplierDescription;
        private List<String> inputValues;
        private List<String> loopNames;
        private boolean missingLoopIndex;
        private String comparisonOperator;
        private String comparisonLeftOperand;
        private String comparisonRightOperand;
        private String stringSearchMethod;
        private String stringSearchTarget;
        private String stringSearchArg;
        private boolean argumentNameMessage;

        /**
         * Create a new builder instance.
         */
        public Builder() {
        }

        /**
         * Set the source category.
         *
         * @param source origin of the candidate.
         * @return this builder for chaining.
         */
        public Builder source(MessageSource source) {
            this.source = source;
            return this;
        }

        /**
         * Set the advice source override.
         *
         * @param adviceSource advice source override.
         * @return this builder for chaining.
         */
        public Builder adviceSource(AdviceSource adviceSource) {
            this.adviceSource = adviceSource;
            return this;
        }

        /**
         * Set the line number where the candidate occurs.
         *
         * @param lineNo line number where the candidate occurs.
         * @return this builder for chaining.
         */
        public Builder lineNo(int lineNo) {
            this.lineNo = lineNo;
            return this;
        }

        /**
         * Set the message text.
         *
         * @param message message text.
         * @return this builder for chaining.
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the original message expression text.
         *
         * @param messageExpr message expression text.
         * @return this builder for chaining.
         */
        public Builder messageExpr(String messageExpr) {
            this.messageExpr = messageExpr;
            return this;
        }

        /**
         * Set the normalised message text.
         *
         * @param normalisedMessage normalised message text used for comparisons.
         * @return this builder for chaining.
         */
        public Builder normalisedMessage(String normalisedMessage) {
            this.normalisedMessage = normalisedMessage;
            return this;
        }

        /**
         * Set the number of placeholders identified in the message.
         *
         * @param placeholderCount number of placeholders identified in the message.
         * @return this builder for chaining.
         */
        public Builder placeholderCount(int placeholderCount) {
            this.placeholderCount = placeholderCount;
            return this;
        }

        /**
         * Set the number of key-value labels in the message.
         *
         * @param keyValueLabelCount number of key-value labels in the message.
         * @return this builder for chaining.
         */
        public Builder keyValueLabelCount(int keyValueLabelCount) {
            this.keyValueLabelCount = keyValueLabelCount;
            return this;
        }

        /**
         * Set whether the message is a compile-time constant.
         *
         * @param constantMessage {@code true} when the message is a compile-time constant.
         * @return this builder for chaining.
         */
        public Builder constantMessage(boolean constantMessage) {
            this.constantMessage = constantMessage;
            return this;
        }

        /**
         * Set whether a message is missing.
         *
         * @param missingMessage {@code true} when a message is missing.
         * @return this builder for chaining.
         */
        public Builder missingMessage(boolean missingMessage) {
            this.missingMessage = missingMessage;
            return this;
        }

        /**
         * Set the missing message kind for fix guidance.
         *
         * @param missingMessageKind missing message kind to guide fixes.
         * @return this builder for chaining.
         */
        public Builder missingMessageKind(MissingMessageKind missingMessageKind) {
            this.missingMessageKind = missingMessageKind;
            return this;
        }

        /**
         * Set whether the throw statement uses a null literal.
         *
         * @param throwNull {@code true} when the throw statement is a null literal.
         * @return this builder for chaining.
         */
        public Builder throwNull(boolean throwNull) {
            this.throwNull = throwNull;
            return this;
        }

        /**
         * Set whether the candidate is an assertAll heading.
         *
         * @param assertAllHeading {@code true} when candidate is an assertAll heading.
         * @return this builder for chaining.
         */
        public Builder assertAllHeading(boolean assertAllHeading) {
            this.assertAllHeading = assertAllHeading;
            return this;
        }

        /**
         * Set whether the message is an AssertJ override.
         *
         * @param assertJOverride {@code true} when the message is an AssertJ override.
         * @return this builder for chaining.
         */
        public Builder assertJOverride(boolean assertJOverride) {
            this.assertJOverride = assertJOverride;
            return this;
        }

        /**
         * Set the trivial supplier description.
         *
         * @param trivialSupplierDescription description of a trivial supplier.
         * @return this builder for chaining.
         */
        public Builder trivialSupplierDescription(String trivialSupplierDescription) {
            this.trivialSupplierDescription = trivialSupplierDescription;
            return this;
        }

        /**
         * Set the input values referenced by the message.
         *
         * @param inputValues values referenced by the message.
         * @return this builder for chaining.
         */
        public Builder inputValues(List<String> inputValues) {
            this.inputValues = inputValues;
            return this;
        }

        /**
         * Set whether the message mirrors the first argument for requireNonNull/requireNotNull.
         *
         * @param argumentNameMessage {@code true} when the message matches the first argument text.
         * @return this builder for chaining.
         */
        public Builder argumentNameMessage(boolean argumentNameMessage) {
            this.argumentNameMessage = argumentNameMessage;
            return this;
        }

        /**
         * Set loop variable names associated with the candidate.
         *
         * @param loopNames loop variable names associated with the candidate.
         * @return this builder for chaining.
         */
        public Builder loopNames(List<String> loopNames) {
            this.loopNames = loopNames;
            return this;
        }

        /**
         * Set whether a loop index is required but missing.
         *
         * @param missingLoopIndex {@code true} when a loop index is required but missing.
         * @return this builder for chaining.
         */
        public Builder missingLoopIndex(boolean missingLoopIndex) {
            this.missingLoopIndex = missingLoopIndex;
            return this;
        }

        /**
         * Set the comparison operator inferred from a comparison.
         *
         * @param comparisonOperator operator inferred from a comparison.
         * @return this builder for chaining.
         */
        public Builder comparisonOperator(String comparisonOperator) {
            this.comparisonOperator = comparisonOperator;
            return this;
        }

        /**
         * Set the left operand name for a comparison.
         *
         * @param comparisonLeftOperand left operand name for a comparison.
         * @return this builder for chaining.
         */
        public Builder comparisonLeftOperand(String comparisonLeftOperand) {
            this.comparisonLeftOperand = comparisonLeftOperand;
            return this;
        }

        /**
         * Set the right operand name for a comparison.
         *
         * @param comparisonRightOperand right operand name for a comparison.
         * @return this builder for chaining.
         */
        public Builder comparisonRightOperand(String comparisonRightOperand) {
            this.comparisonRightOperand = comparisonRightOperand;
            return this;
        }

        /**
         * Set the string search method name.
         *
         * @param stringSearchMethod string search method name.
         * @return this builder for chaining.
         */
        public Builder stringSearchMethod(String stringSearchMethod) {
            this.stringSearchMethod = stringSearchMethod;
            return this;
        }

        /**
         * Set the string search target name.
         *
         * @param stringSearchTarget string search target name.
         * @return this builder for chaining.
         */
        public Builder stringSearchTarget(String stringSearchTarget) {
            this.stringSearchTarget = stringSearchTarget;
            return this;
        }

        /**
         * Set the string search argument value.
         *
         * @param stringSearchArg string search argument value.
         * @return this builder for chaining.
         */
        public Builder stringSearchArg(String stringSearchArg) {
            this.stringSearchArg = stringSearchArg;
            return this;
        }

        /**
         * Build an immutable {@link MessageCandidate}.
         *
         * @return newly created candidate.
         */
        public MessageCandidate build() {
            return new MessageCandidate(this);
        }
    }
}
