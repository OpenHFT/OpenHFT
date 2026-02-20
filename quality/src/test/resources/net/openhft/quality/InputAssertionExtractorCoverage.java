/*
 * Test input for MeaningfulMessageCheck assertion extractor coverage.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;

public class InputAssertionExtractorCoverage {

    public void qualifiedAssertion() {
        Assertions.assertTrue(true, "cache entry should be ready for ingestion because loaders are warm");
    }

    public void loopAssignedIndex() {
        int idx = 0;
        for (idx = 0; idx < 2; idx++) {
            assertEquals(1, 1, "cache entry should be ready for persistence");
        }
    }

    public void trivialSupplierFormat(String value) {
        assertEquals(1, 1, () -> String.format("cache entry should be ready for %s", value));
    }
}
