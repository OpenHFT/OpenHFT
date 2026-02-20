/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckThrowFixture {

    public void triggerMissingMessage() {
        throw new IllegalStateException();
    }

    public void triggerThrowNull() {
        throw null;
    }

    public void triggerRestatesDerived() {
        throw new IllegalStateException("empty");
    }

    public void triggerContextless() {
        throw new IllegalStateException("validation");
    }

    public void triggerIndexOnly() {
        throw new IllegalStateException("index 7");
    }

    public void triggerLongWord() {
        throw new IllegalStateException(
                "operation uses supercalifragilisticexpialidocioussupercalifragilistic token");
    }

    public void triggerTooLong() {
        throw new IllegalStateException(
                "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 "
                        + "word11 word12 word13 word14 word15 word16 word17 word18 word19 word20 "
                        + "word21 word22 word23 word24 word25 word26 word27 word28 word29 word30 "
                        + "word31 word32 word33 word34 word35 word36 word37 word38 word39 word40 "
                        + "word41 word42 word43");
    }

    public void triggerGeneric() {
        throw new IllegalStateException("expected");
    }

    public void triggerRestatesAssertion() {
        throw new IllegalStateException("should be true");
    }

    public void triggerWhitespaceRun() {
        throw new IllegalStateException("order  should persist");
    }

    public void triggerRedundantLineNumber() {
        throw new IllegalStateException("error at line 42");
    }

    public void triggerTooFewMeaningful() {
        throw new IllegalStateException("the the");
    }

    public void triggerDuplicateFirst() {
        throw new IllegalStateException("throw duplicate message");
    }

    public void triggerDuplicateSecond() {
        throw new IllegalStateException("throw duplicate message");
    }

    public void triggerTooShort() {
        throw new IllegalStateException("cache");
    }
}
