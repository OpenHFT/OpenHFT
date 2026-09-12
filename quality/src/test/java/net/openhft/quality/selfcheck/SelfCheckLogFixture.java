/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import java.util.logging.Logger;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckLogFixture {
    private static final Logger LOGGER = Logger.getLogger(SelfCheckLogFixture.class.getName());

    public void triggerMissingMessage() {
        LOGGER.info((String) null);
    }

    public void triggerContextless() {
        LOGGER.info("validation");
    }

    public void triggerIndexOnly() {
        LOGGER.info("index 7");
    }

    public void triggerLongWord() {
        LOGGER.info("operation uses supercalifragilisticexpialidocioussupercalifragilistic token");
    }

    public void triggerTooLong() {
        LOGGER.info(
                "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 "
                        + "word11 word12 word13 word14 word15 word16 word17 word18 word19 word20 "
                        + "word21 word22 word23 word24 word25 word26 word27 word28 word29 word30 "
                        + "word31 word32 word33 word34 word35 word36 word37 word38 word39 word40 "
                        + "word41 word42 word43");
    }

    public void triggerTrivialSupplier() {
        LOGGER.info(() -> "cheap supplier message");
    }

    public void triggerGeneric() {
        LOGGER.info("expected");
    }

    public void triggerWhitespaceRun() {
        LOGGER.info("order  should persist");
    }

    public void triggerRedundantLineNumber() {
        LOGGER.info("error at line 42");
    }

    public void triggerTooFewMeaningful() {
        LOGGER.info("the the the the");
    }

    public void triggerMissingSubject() {
        LOGGER.info("should handle input");
    }

    public void triggerDuplicateFirst() {
        LOGGER.info("log duplicate message detail");
    }

    public void triggerDuplicateSecond() {
        LOGGER.info("log duplicate message detail");
    }

    public void triggerTooShort() {
        LOGGER.info("cache");
    }
}
