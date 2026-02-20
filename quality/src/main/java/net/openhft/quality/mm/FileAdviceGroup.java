/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import static java.util.Objects.requireNonNull;

/**
 * Grouped file-level advice block.
 */
public final class FileAdviceGroup {
    private final AdviceId adviceId;
    private final AdviceText adviceText;
    private final int rank;
    private final FileAdviceDetails details;

    public FileAdviceGroup(AdviceId adviceId, AdviceText adviceText, int rank,
                           FileAdviceDetails details) {
        this.adviceId = requireNonNull(adviceId, "adviceId");
        this.adviceText = requireNonNull(adviceText, "adviceText");
        this.rank = rank;
        this.details = requireNonNull(details, "details");
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

    public FileAdviceDetails details() {
        return details;
    }
}
