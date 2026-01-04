/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Collects per-line violations and emits the highest priority ones.
 */
public class ViolationCollector {
    private final Map<Integer, Violation> pending = new HashMap<>();
    private final SuppressionTracker suppressionTracker;

    /**
     * Create a collector using the supplied suppression tracker.
     *
     * @param suppressionTracker suppression tracker to consult, or {@code null}.
     */
    public ViolationCollector(SuppressionTracker suppressionTracker) {
        this.suppressionTracker = suppressionTracker;
    }

    /**
     * Record a violation for a line, keeping the highest priority for that line.
     *
     * @param lineNo line number.
     * @param ruleId rule identifier.
     * @param args   message arguments.
     * @return {@code true} if the violation is recorded.
     */
    public boolean record(int lineNo, RuleId ruleId, Object... args) {
        requireNonNull(ruleId, "ruleId is null");
        if (suppressionTracker != null && suppressionTracker.isSuppressed(ruleId)) {
            return false;
        }
        Violation existing = pending.get(lineNo);
        if (existing == null || isHigherPriority(ruleId, existing.ruleId())) {
            pending.put(lineNo, new Violation(lineNo, ruleId, args));
        }
        return true;
    }

    /**
     * Emit all collected violations to the supplied check.
     *
     * @param check checkstyle check used to log violations.
     */
    public void flush(AbstractCheck check) {
        if (pending.isEmpty()) {
            return;
        }
        List<Integer> lines = new ArrayList<>(pending.keySet());
        Collections.sort(lines);
        for (Integer line : lines) {
            Violation violation = pending.get(line);
            if (violation != null) {
                check.log(line, violation.ruleId().messageKey(), violation.args());
            }
        }
    }

    /**
     * Clear all pending violations.
     */
    public void clear() {
        pending.clear();
    }

    Map<Integer, Violation> pendingForTesting() {
        return new HashMap<>(pending);
    }

    private boolean isHigherPriority(RuleId candidate, RuleId existing) {
        requireNonNull(existing);
        requireNonNull(candidate);
        if (candidate.priority() != existing.priority()) {
            return candidate.priority() < existing.priority();
        }
        int candidateLength = candidate.code().length();
        int existingLength = existing.code().length();
        if (candidateLength != existingLength) {
            return candidateLength < existingLength;
        }
        return candidate.order() < existing.order();
    }
}
