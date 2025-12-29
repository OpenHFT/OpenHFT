/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Context passed to rules during evaluation of a message candidate.
 */
public final class MessageContext {
    private final MessageCandidate candidate;
    private final MessageMetrics metrics;
    private final String currentClassName;
    private final String currentMethodName;
    private final boolean verbose;
    private final MessageRuleSupport ruleSupport;
    private final SuppressionTracker suppressionTracker;

    /**
     * Create a new message context.
     *
     * @param candidate          extracted message candidate.
     * @param metrics            computed message metrics, or {@code null} if unavailable.
     * @param currentClassName   enclosing class name.
     * @param currentMethodName  enclosing method name.
     * @param verbose            {@code true} when verbose reporting is enabled.
     * @param ruleSupport        shared rule support state.
     * @param suppressionTracker suppression tracker for this file.
     */
    public MessageContext(MessageCandidate candidate, MessageMetrics metrics,
                          String currentClassName, String currentMethodName,
                          boolean verbose, MessageRuleSupport ruleSupport,
                          SuppressionTracker suppressionTracker) {
        this.candidate = candidate;
        this.metrics = metrics;
        this.currentClassName = currentClassName;
        this.currentMethodName = currentMethodName;
        this.verbose = verbose;
        this.ruleSupport = ruleSupport;
        this.suppressionTracker = suppressionTracker;
    }

    /**
     * Return the message candidate.
     *
     * @return message candidate.
     */
    public MessageCandidate candidate() {
        return candidate;
    }

    /**
     * Return computed message metrics.
     *
     * @return message metrics, or {@code null} if unavailable.
     */
    public MessageMetrics metrics() {
        return metrics;
    }

    /**
     * Return the current class name.
     *
     * @return current class name.
     */
    public String currentClassName() {
        return currentClassName;
    }

    /**
     * Return the current method name.
     *
     * @return current method name.
     */
    public String currentMethodName() {
        return currentMethodName;
    }

    /**
     * Return whether verbose reporting is enabled.
     *
     * @return {@code true} when verbose reporting is enabled.
     */
    public boolean verbose() {
        return verbose;
    }

    /**
     * Return shared rule support.
     *
     * @return rule support.
     */
    public MessageRuleSupport ruleSupport() {
        return ruleSupport;
    }

    /**
     * Return the suppression tracker.
     *
     * @return suppression tracker.
     */
    public SuppressionTracker suppressionTracker() {
        return suppressionTracker;
    }
}
