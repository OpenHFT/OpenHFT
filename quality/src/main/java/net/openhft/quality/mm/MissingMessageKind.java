/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Categorises missing message requirements for fix suggestions.
 */
public enum MissingMessageKind {
    RETURN_NULL,
    SYSTEM_CALL,
    RUNTIME_CALL
}
