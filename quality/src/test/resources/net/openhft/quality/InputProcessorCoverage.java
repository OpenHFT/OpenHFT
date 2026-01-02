/*
 * Test input for MeaningfulMessageCheck processor coverage.
 */
package net.openhft.quality;

import java.text.MessageFormat;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputProcessorCoverage {

    @DisplayName("cache entry should be ready for lookup")
    public void displayNameMessage() {
    }

    public void templateMessages(String value) {
        assertEquals(1, 1, String.format("cache entry should be ready for %s", value));
        assertEquals(1, 1, String.format(Locale.UK, "cache entry should be ready for export"));
        assertEquals(1, 1, MessageFormat.format("cache entry should be ready for {0}", value));
        assertEquals(1, 1, "cache entry should be ready for validation %s".formatted(value));
    }

    public void qualifiedAssertion() {
        Assertions.assertTrue(true, "cache entry should be ready for ingestion");
    }

    public void preconditionMessage(Object input) {
        requireNonNull(input, "cache entry should be ready for retention");
    }

    public void throwMessage() {
        throw new IllegalStateException(new String("cache entry should be ready for logging"));
    }

    public static class Inner {
        public void innerMessage() {
            Assertions.assertTrue(true, "cache entry should be ready for approval");
        }
    }
}
