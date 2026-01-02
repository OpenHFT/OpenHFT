/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Intentional MeaningfulMessage violations for QualityCheckstyleSelfTest.
 * Keep these messages synchronised with the expected rule counts in that test.
 */
public final class SelfCheckFixture {
    private static final Logger LOGGER = Logger.getLogger(SelfCheckFixture.class.getName());

    private static <T> T requirePresent(T value, Supplier<String> messageSupplier) {
        if (value == null) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
        return value;
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public String triggerMissingMessage() {
        throw new IllegalStateException();
    }

    public String triggerRestatesDerived(String value) {
        return requireNonNull(value, "empty");
    }

    public String triggerContextless(String value) {
        return requireNonNull(value, "validation");
    }

    public String triggerIndexOnly(String value) {
        return requireNonNull(value, "index 7");
    }

    public String triggerLongWord(String value) {
        return requireNonNull(value,
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghi");
    }

    public void triggerTrivialSupplier(String value) {
        requirePresent(value, () -> "cheap supplier message");
    }

    public String triggerGeneric(String value) {
        return requireNonNull(value, "expected");
    }

    public void triggerRedundantClass(boolean ok) {
        assertCondition(ok, "SelfCheckFixture failed");
    }

    public void triggerRedundantMethod(boolean ok) {
        assertCondition(ok, "triggerRedundantMethod failed");
    }

    public void triggerRedundantLine(boolean ok) {
        assertCondition(ok, "line 42");
    }

    public void triggerRestatesAssertion(boolean ok) {
        assertCondition(ok, "should be true");
    }

    public String triggerWhitespaceRun(String value) {
        return requireNonNull(value, "cache  entry missing");
    }

    public String triggerTooShort(String value) {
        return requireNonNull(value, "message body missing");
    }

    public String triggerTooLong(String value) {
        return requireNonNull(value,
                "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 "
                        + "word11 word12 word13 word14 word15 word16 word17 word18 word19 word20 "
                        + "word21 word22 word23 word24 word25 word26 word27 word28 word29 word30 "
                        + "word31 word32 word33 word34 word35 word36 word37 word38 word39 word40 "
                        + "word41 word42 word43");
    }

    public String triggerTooFewMeaningful(String value) {
        return requireNonNull(value, "expected value should match");
    }

    public void triggerMissingSubjectLog() {
        LOGGER.info("should handle input");
    }

    public String triggerDuplicateFirst(String value) {
        return requireNonNull(value, "cache entry expired unexpectedly");
    }

    public String triggerDuplicateSecond(String value) {
        return requireNonNull(value, "cache entry expired unexpectedly");
    }
}
