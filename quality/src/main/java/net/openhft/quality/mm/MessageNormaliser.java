/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Objects;

/**
 * Normalises message text for comparison and de-duplication.
 */
public final class MessageNormaliser {

    private MessageNormaliser() {
    }

    /**
     * Normalise a message by trimming, lower-casing, and collapsing whitespace.
     *
     * @param message message text to normalise.
     * @return normalised message text.
     */
    public static String normalise(String message) {
        Objects.requireNonNull(message);
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        StringBuilder normalised = new StringBuilder(trimmed.length());
        boolean inWhitespace = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (!inWhitespace) {
                    normalised.append(' ');
                    inWhitespace = true;
                }
            } else {
                normalised.append(Character.toLowerCase(ch));
                inWhitespace = false;
            }
        }
        return normalised.toString();
    }
}
