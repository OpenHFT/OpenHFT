/*
 * Test input for MeaningfulMessageCheck cheap supplier descriptions.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputCheapSupplierDescriptions {

    public void testCheapSuppliers() {
        String message = buildMessage();
        String prefix = "prefix";
        String suffix = "suffix";

        assertTrue(true, () -> message);
        assertTrue(true, () -> prefix + suffix);
        assertTrue(true, () -> prefix + message);
    }

    private String buildMessage() {
        return "cache entry should be ready for persistence";
    }
}
