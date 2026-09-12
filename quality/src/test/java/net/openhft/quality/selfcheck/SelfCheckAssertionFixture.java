/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckAssertionFixture {

    public void triggerMissingMessage() {
        assertTrue(true);
    }

    public void triggerAssertAllHeading() {
        assertAll("checks",
                () -> assertTrue(true, "order should remain stable after update"));
    }

    public void triggerAssertJOverride() {
        new AssertJStub().withFailMessage("values should match");
    }

    public void triggerTrivialSupplier() {
        assertTrue(true, () -> "cheap supplier message");
    }

    public void triggerDuplicatesInput() {
        String expected = "admin";
        String actual = "user";
        assertEquals(expected, actual, "admin");
    }

    public void triggerMissingLoopIndex() {
        for (int i = 0; i < 1; i++) {
            assertTrue(true, "loop value mismatch detected");
        }
    }

    public void triggerMissingComparisonValues() {
        int a = 1;
        int b = 2;
        assertTrue(a > b, "a should be greater than b");
    }

    public void triggerMissingStringSearchValue() {
        String text = "hello@example.com";
        assertTrue(text.contains("@"), "email should contain symbol");
    }

    public void triggerDuplicateFirst() {
        assertTrue(true, "assertion duplicate message");
    }

    public void triggerDuplicateSecond() {
        assertTrue(true, "assertion duplicate message");
    }

    public void triggerRedundantClassName() {
        assertTrue(true, "SelfCheckAssertionFixture failed");
    }

    public void triggerRedundantMethodName() {
        assertTrue(true, "triggerRedundantMethodName failed");
    }

    public void triggerRedundantLineNumber() {
        assertTrue(true, "error at line 42");
    }

    public void triggerGenericMessage() {
        assertTrue(true, "expected");
    }

    public void triggerRestatesAssertion() {
        assertTrue(true, "should be true");
    }

    public void triggerIndexOnly() {
        assertTrue(true, "index 7");
    }

    public void triggerContextless() {
        assertTrue(true, "validation");
    }

    public void triggerRestatesDerived() {
        assertTrue(true, "empty");
    }

    public void triggerMissingSubject() {
        assertTrue(true, "should handle input");
    }

    public void triggerWhitespaceRun() {
        assertTrue(true, "order  should persist");
    }

    public void triggerLongWord() {
        assertTrue(true, "operation uses supercalifragilisticexpialidocioussupercalifragilistic token");
    }

    public void triggerTooShort() {
        assertTrue(true, "cache");
    }

    public void triggerTooLong() {
        assertTrue(true,
                "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 "
                        + "word11 word12 word13 word14 word15 word16 word17 word18 word19 word20 "
                        + "word21 word22 word23 word24 word25 word26 word27 word28 word29 word30 "
                        + "word31 word32 word33 word34 word35 word36 word37 word38 word39 word40 "
                        + "word41 word42 word43");
    }

    public void triggerTooFewMeaningful() {
        assertTrue(true, "the the the the");
    }

    private static final class AssertJStub {
        private AssertJStub withFailMessage(String message) {
            return this;
        }
    }
}
