/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import static java.util.Objects.requireNonNull;

/**
 * Advice text definition loaded from properties.
 */
public final class AdviceText {
    private final AdviceId adviceId;
    private final String title;
    private final String intentIntro;
    private final String intentOutro;
    private final String hintA;
    private final String hintB;
    private final String checklist;
    private final String antiPatterns;
    private final String verbose;

    public AdviceText(AdviceId adviceId, String title, String intentIntro,
                      String intentOutro, String hintA, String hintB,
                      String checklist, String antiPatterns, String verbose) {
        this.adviceId = requireNonNull(adviceId, "adviceId");
        this.title = requireNonNull(title, "title");
        this.intentIntro = requireNonNull(intentIntro, "intentIntro");
        this.intentOutro = requireNonNull(intentOutro, "intentOutro");
        this.hintA = requireNonNull(hintA, "hintA");
        this.hintB = requireNonNull(hintB, "hintB");
        this.checklist = requireNonNull(checklist, "checklist");
        this.antiPatterns = requireNonNull(antiPatterns, "antiPatterns");
        this.verbose = requireNonNull(verbose, "verbose");
    }

    public AdviceId adviceId() {
        return adviceId;
    }

    public String title() {
        return title;
    }

    public String intentIntro() {
        return intentIntro;
    }

    public String intentOutro() {
        return intentOutro;
    }

    public String hintA() {
        return hintA;
    }

    public String hintB() {
        return hintB;
    }

    public String checklist() {
        return checklist;
    }

    public String antiPatterns() {
        return antiPatterns;
    }

    public String verbose() {
        return verbose;
    }
}
