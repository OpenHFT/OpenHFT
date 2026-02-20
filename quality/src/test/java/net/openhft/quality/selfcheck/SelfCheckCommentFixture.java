/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckCommentFixture {

    public void triggerMissingCommentMessage() {
        System.exit(0);
    }

    public void triggerTooShortComment() {
        // cache
        System.getProperty("user.dir");
    }

    public void triggerTooFewMeaningfulComment() {
        // the the the the
        System.getProperty("user.home");
    }

    public void triggerMissingSubjectComment() {
        // should handle input
        System.getProperty("user.name");
    }

    public void triggerDuplicateCommentFirst() {
        // comment duplicate reason supplied
        System.getProperty("os.name");
    }

    public void triggerDuplicateCommentSecond() {
        // comment duplicate reason supplied
        System.getProperty("os.version");
    }
}
