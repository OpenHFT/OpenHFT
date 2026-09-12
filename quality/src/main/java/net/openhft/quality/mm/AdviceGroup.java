/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Grouped line-level advice for a single AdviceId.
 */
public final class AdviceGroup {
    private final AdviceId adviceId;
    private final AdviceText adviceText;
    private final int rank;
    private final List<AdviceOccurrence> occurrences;

    public AdviceGroup(AdviceId adviceId, AdviceText adviceText, int rank,
                       List<AdviceOccurrence> occurrences) {
        this.adviceId = requireNonNull(adviceId, "adviceId");
        this.adviceText = requireNonNull(adviceText, "adviceText");
        this.rank = rank;
        this.occurrences = Collections.unmodifiableList(new java.util.ArrayList<>(occurrences));
    }

    public AdviceId adviceId() {
        return adviceId;
    }

    public AdviceText adviceText() {
        return adviceText;
    }

    public int rank() {
        return rank;
    }

    public List<AdviceOccurrence> occurrences() {
        return occurrences;
    }
}
