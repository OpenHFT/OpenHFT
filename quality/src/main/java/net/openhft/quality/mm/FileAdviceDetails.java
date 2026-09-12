/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * File-level advice details with optional metrics payloads.
 */
public final class FileAdviceDetails {
    private final AdviceId adviceId;
    private final int lineNo;
    private final List<String> items;
    private final Integer messageCount;
    private final Integer purposeCueCount;
    private final Integer expectedPurposeCueCount;
    private final Integer totalMessageCount;
    private final Double entropy;
    private final Double minEntropy;

    public FileAdviceDetails(AdviceId adviceId, int lineNo, List<String> items,
                             Integer messageCount, Integer purposeCueCount,
                             Integer expectedPurposeCueCount, Integer totalMessageCount,
                             Double entropy, Double minEntropy) {
        this.adviceId = requireNonNull(adviceId, "adviceId");
        this.lineNo = lineNo;
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(new java.util.ArrayList<>(items));
        this.messageCount = messageCount;
        this.purposeCueCount = purposeCueCount;
        this.expectedPurposeCueCount = expectedPurposeCueCount;
        this.totalMessageCount = totalMessageCount;
        this.entropy = entropy;
        this.minEntropy = minEntropy;
    }

    public AdviceId adviceId() {
        return adviceId;
    }

    public int lineNo() {
        return lineNo;
    }

    public List<String> items() {
        return items;
    }

    public Integer messageCount() {
        return messageCount;
    }

    public Integer purposeCueCount() {
        return purposeCueCount;
    }

    public Integer expectedPurposeCueCount() {
        return expectedPurposeCueCount;
    }

    public Integer totalMessageCount() {
        return totalMessageCount;
    }

    public Double entropy() {
        return entropy;
    }

    public Double minEntropy() {
        return minEntropy;
    }
}
