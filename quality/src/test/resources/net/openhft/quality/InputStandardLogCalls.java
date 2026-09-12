/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InputStandardLogCalls {

    private static final Logger LOG = LoggerFactory.getLogger(InputStandardLogCalls.class);

    // null message without throwable -> MISSING_MESSAGE
    void logNullMessage() {
        LOG.error(null); // violation
    }

    // null message with throwable -> suppressed
    void logNullWithThrowable() {
        LOG.error(null, new RuntimeException("cause"));
    }

    // method reference supplier -> MISSING_MESSAGE (non-constant)
    void logMethodRefSupplier() {
        LOG.atInfo().log(this::toString); // MM-reason: method reference supplier
    }

    // normal message template -> checks quality
    void logNormalMessage() {
        LOG.info("Processing {} items for batch {}", 10, "alpha");
    }

    // blank message with throwable -> suppressed
    void logBlankWithThrowable() {
        LOG.warn("", new RuntimeException("cause"));
    }

    // message template with format placeholders
    void logFormatString() {
        LOG.info(String.format("Loaded %d entries from %s", 42, "cache"));
    }
}
