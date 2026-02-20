/*
 * Input file that should not produce MMUnhandled for local helpers or supplier messages.
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputUnhandledSkipped {
    @Test
    @DisplayName("Supplier message is used for last argument because it should not be unhandled")
    public void supplierMessageUsesLastArgument() {
        String actual = "alpha";
        Supplier<String> message = () -> "expected alpha because test exercises supplier messages";
        assertEquals("alpha", actual, message);
    }

    @Test
    @DisplayName("Java assert message supplied by method call is accepted without unhandled warning")
    public void javaAssertUsesMethodCallMessage() {
        int value = 1;
        assert value > 0 : failDescription(value);
    }

    @Test
    @DisplayName("Local assertThrows helper is ignored by assertion extraction rules")
    public void localHelperNamedAssertThrowsIsIgnored() {
        assertThrows("local helper should throw because empty store rejects invalid input",
                IllegalArgumentException.class, () -> {
                    throw new IllegalArgumentException("invalid input");
                });
    }

    private String failDescription(int value) {
        return "value must be positive because callers expect a valid offset, got " + value;
    }

    private void assertThrows(String message, Class<? extends Throwable> type, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("Unexpected exception type because helper expects: " + message, t);
        }
        throw new AssertionError("Expected exception " + type + " because " + message);
    }
}
