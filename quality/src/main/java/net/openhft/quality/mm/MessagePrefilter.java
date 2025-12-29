/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Prefilters messages that are obviously not user-facing text.
 */
public final class MessagePrefilter {
    private static final Pattern ARRAY_VALUE_PATTERN = Pattern.compile(
            "^\\[[^\\[\\]]*,[^\\[\\]]*\\]$"
    );
    private static final Pattern ESCAPE_ONLY_PATTERN = Pattern.compile(
            "^(\\\\+|\\\\n|\\\\t|\\\\r|\\s*)+$"
    );
    private static final Pattern GENERATED_CLASS_NAME_PATTERN = Pattern.compile(
            "^[A-Z][a-zA-Z0-9]*([A-Z][a-z0-9]+){4,}[A-Za-z0-9]*$"
    );

    /**
     * Create a prefilter instance.
     */
    public MessagePrefilter() {
    }

    /**
     * Check whether a message should be skipped from rule evaluation.
     *
     * @param message message text to test.
     * @return {@code true} if the message should be skipped.
     */
    public boolean shouldSkip(String message) {
        Objects.requireNonNull(message);
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.startsWith("{")
                || trimmed.startsWith("[{")
                || trimmed.startsWith("[\"")
                || trimmed.startsWith("[[")) {
            return true;
        }
        if (looksLikeClassName(trimmed)) {
            return true;
        }
        if (looksLikeDataValue(trimmed)) {
            return true;
        }
        if (ARRAY_VALUE_PATTERN.matcher(trimmed).matches()) {
            return true;
        }
        if (ESCAPE_ONLY_PATTERN.matcher(trimmed).matches()) {
            return true;
        }
        return GENERATED_CLASS_NAME_PATTERN.matcher(trimmed).matches();
    }

    private boolean looksLikeDataValue(String text) {
        boolean hasNewline = text.contains("\n") || text.contains("\\n");
        if (hasNewline && text.contains(":")) {
            return true;
        }
        if (hasNewline && text.contains("=")) {
            return true;
        }
        if (text.startsWith("!")) {
            return true;
        }
        if (hasNewline) {
            int newlineCount = 0;
            for (int i = 0; i < text.length() - 1; i++) {
                if (text.charAt(i) == '\\' && text.charAt(i + 1) == 'n') {
                    newlineCount++;
                } else if (text.charAt(i) == '\n') {
                    newlineCount++;
                }
            }
            return newlineCount >= 2;
        }
        return false;
    }

    private boolean looksLikeClassName(String text) {
        if (!text.contains(".") || text.contains(" ")) {
            return false;
        }
        String[] parts = text.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || !Character.isJavaIdentifierStart(part.charAt(0))) {
                return false;
            }
            for (int i = 1; i < part.length(); i++) {
                if (!Character.isJavaIdentifierPart(part.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
