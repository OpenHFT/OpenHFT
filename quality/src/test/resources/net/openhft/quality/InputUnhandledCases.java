/*
 * Test input for unhandled extraction cases.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputUnhandledCases {
    public void unhandledLambdaMessage() {
        assertTrue(() -> true, () -> String.valueOf(System.nanoTime()));
    }
}
