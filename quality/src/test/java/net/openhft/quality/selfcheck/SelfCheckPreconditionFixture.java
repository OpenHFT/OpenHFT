/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import static java.util.Objects.requireNonNull;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckPreconditionFixture {
    private final String value = "value";

    public void triggerMissingMessage() {
        requireNonNull(value);
    }

    public void triggerRestatesDerived() {
        requireNonNull(value, "empty");
    }

    public void triggerContextless() {
        requireNonNull(value, "validation");
    }

    public void triggerIndexOnly() {
        requireNonNull(value, "index 7");
    }

    public void triggerLongWord() {
        requireNonNull(value,
                "operation uses supercalifragilisticexpialidocioussupercalifragilistic token");
    }

    public void triggerTooLong() {
        requireNonNull(value,
                "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 "
                        + "word11 word12 word13 word14 word15 word16 word17 word18 word19 word20 "
                        + "word21 word22 word23 word24 word25 word26 word27 word28 word29 word30 "
                        + "word31 word32 word33 word34 word35 word36 word37 word38 word39 word40 "
                        + "word41 word42 word43");
    }

    public void triggerTrivialSupplier() {
        requireNonNull(value, () -> "cheap supplier message");
    }

    public void triggerGeneric() {
        requireNonNull(value, "expected");
    }

    public void triggerRestatesAssertion() {
        requireNonNull(value, "should be true");
    }

    public void triggerWhitespaceRun() {
        requireNonNull(value, "order  should persist");
    }

    public void triggerRedundantLineNumber() {
        requireNonNull(value, "error at line 42");
    }

    public void triggerTooFewMeaningful() {
        requireNonNull(value, "the the the the");
    }

    public void triggerDuplicateFirst() {
        requireNonNull(value, "precondition duplicate message");
    }

    public void triggerDuplicateSecond() {
        requireNonNull(value, "precondition duplicate message");
    }

    public void triggerTooShort() {
        requireNonNull(value, "cache");
    }
}
