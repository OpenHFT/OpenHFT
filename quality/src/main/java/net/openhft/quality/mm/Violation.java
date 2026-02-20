/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Arrays;

/**
 * Immutable representation of a rule violation.
 */
public final class Violation {
    private final int lineNo;
    private final RuleId ruleId;
    private final Object[] args;

    /**
     * Create a violation record.
     *
     * @param lineNo line number where the violation occurred.
     * @param ruleId rule identifier.
     * @param args   message arguments associated with the violation.
     */
    public Violation(int lineNo, RuleId ruleId, Object[] args) {
        this.lineNo = lineNo;
        this.ruleId = ruleId;
        this.args = args == null ? null : Arrays.copyOf(args, args.length);
    }

    /**
     * Return the line number where the violation occurred.
     *
     * @return line number.
     */
    public int lineNo() {
        return lineNo;
    }

    /**
     * Return the rule identifier.
     *
     * @return rule identifier.
     */
    public RuleId ruleId() {
        return ruleId;
    }

    /**
     * Return message arguments for the violation.
     *
     * @return message argument array.
     */
    public Object[] args() {
        return args == null ? null : Arrays.copyOf(args, args.length);
    }
}
