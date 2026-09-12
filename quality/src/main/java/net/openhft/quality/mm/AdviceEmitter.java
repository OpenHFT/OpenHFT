/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Receives rule triggers for advice aggregation.
 */
public interface AdviceEmitter {
    /**
     * Record a triggered rule for advice aggregation.
     *
     * @param context message context.
     * @param ruleId  triggered rule identifier.
     */
    void record(MessageContext context, RuleId ruleId);
}
