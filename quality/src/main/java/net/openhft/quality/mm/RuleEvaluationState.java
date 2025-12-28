/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Tracks rule evaluation state for a single candidate.
 */
public final class RuleEvaluationState {
    private boolean warningFired;
    private boolean stopProcessing;

    /**
     * Create a new evaluation state.
     */
    public RuleEvaluationState() {
    }

    /**
     * Return whether a warning has been recorded.
     *
     * @return {@code true} if a warning has been recorded.
     */
    public boolean warningFired() {
        return warningFired;
    }

    /**
     * Mark that a warning has been recorded.
     */
    public void markWarningFired() {
        warningFired = true;
    }

    /**
     * Return whether rule processing should stop.
     *
     * @return {@code true} if processing should stop.
     */
    public boolean shouldStopProcessing() {
        return stopProcessing;
    }

    /**
     * Request that rule processing stops after the current rule.
     */
    public void requestStopProcessing() {
        stopProcessing = true;
    }
}
