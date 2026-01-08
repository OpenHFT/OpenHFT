/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Value object for extracted message templates.
 */
public final class MessageTemplate {
    private final String message;
    private final int placeholderCount;
    private final boolean fromFormatCall;

    /**
     * Create a message template.
     *
     * @param message          template message text.
     * @param placeholderCount number of placeholders in the template.
     * @param fromFormatCall   {@code true} if derived from a format call.
     */
    public MessageTemplate(String message, int placeholderCount, boolean fromFormatCall) {
        this.message = message;
        this.placeholderCount = placeholderCount;
        this.fromFormatCall = fromFormatCall;
    }

    /**
     * Return the template message text.
     *
     * @return template message text.
     */
    public String message() {
        return message;
    }

    /**
     * Return the placeholder count.
     *
     * @return placeholder count.
     */
    public int placeholderCount() {
        return placeholderCount;
    }

    /**
     * Return whether the template comes from a format call.
     *
     * @return {@code true} if derived from a format call.
     */
    public boolean fromFormatCall() {
        return fromFormatCall;
    }
}
