/*
 * Test input for MeaningfulMessageCheck assertion branches.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

public class InputAssertionBranchCoverage {

    public void testTypecastMessages() {
        assertEquals(1, 1, (String) "comparison");
        assertEquals(1, 1, (String) null);
        assertTrue(true, (String) "validation");
    }

    public void testSupplierMessagePresence() {
        Supplier<String> supplier = () -> "cache entry should be ready for replay";
        assertDoesNotThrow(() -> {
        }, supplier);
    }
}
