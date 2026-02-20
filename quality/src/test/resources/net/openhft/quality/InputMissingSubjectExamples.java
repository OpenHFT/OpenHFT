/*
 * Test input for missing subject and too short message examples.
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * malformed input: partial character at end
 */
public class InputMissingSubjectExamples {

    @Test
    @DisplayName("Missing subject assertions should report rule")
    void missingSubjectAssertions() {
        assertTrue(false, "should emit missing message");
        assertTrue(false, "should use comment source");
        assertTrue(false, "should not emit unhandled warning");
        assertTrue(false, "should not emit missing message");
        assertTrue(false, "should throw npe for null");
        assertTrue(false, "should emit one message candidate");
        assertTrue(false, "should emit one candidate");
    }

    @Test
    @DisplayName("skipped on windows/wsl")
    void skippedOnWindowsWsl() {
    }

    @Test
    @DisplayName("tradable")
    void tradable() {
    }

    @Test
    @DisplayName("ask indicative")
    void askIndicative() {
    }

    @Test
    @DisplayName("bid indicative")
    void bidIndicative() {
    }

    @Test
    @DisplayName("assumes synchronous")
    void assumesSynchronous() {
    }

    @Test
    @DisplayName("mid")
    void mid() {
    }

    @Test
    @DisplayName("countpaused updated after pauses ({})")
    void countpausedUpdatedAfterPauses() {
    }
}
