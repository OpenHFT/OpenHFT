/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 *
 * Test input for parameter-delegation patterns where a helper method
 * receives a String message parameter and forwards it to an assertion.
 * These should NOT produce MMUnhandled because the message quality is
 * the caller's responsibility — the helper is just a delegation layer.
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputParameterDelegation {

    // --- Helper methods that delegate message parameters to assertions ---

    private void assertSizeEquals(int expected, int actual, String message) {
        assertEquals(expected, actual, message);
    }

    private void assertExitCode(int expectedExit, int actualExit, String exitMsg) {
        assertEquals(expectedExit, actualExit, exitMsg);
    }

    private void assertContains(String haystack, String needle, String reason) {
        assertTrue(haystack.contains(needle), reason);
    }

    // --- Test methods that call the helpers with literal messages ---

    @Test
    @DisplayName("helper delegation with assertEquals should not produce unhandled warning")
    public void helperDelegationEqualsDoesNotProduceUnhandled() {
        assertSizeEquals(3, 3, "fixture should contain three test cases because the scenario defines three sections");
    }

    @Test
    @DisplayName("helper delegation with assertTrue should not produce unhandled warning")
    public void helperDelegationTrueDoesNotProduceUnhandled() {
        assertContains("hello world", "hello", "greeting should contain hello because the input starts with it");
    }

    @Test
    @DisplayName("helper delegation for exit code should not produce unhandled warning")
    public void helperDelegationExitCodeDoesNotProduceUnhandled() {
        assertExitCode(0, 0, "process should exit with code zero because the command completed successfully");
    }
}
