/*
 * Test input for MeaningfulMessageCheck loop index assignment handling.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputLoopAssignmentIndex {

    private int position;

    public void testAssignedFieldIndex() {
        for (this.position = 0; this.position < 2; this.position++) {
            assertEquals(1, 1, "cache entry should be ready for replay");
        }
    }
}
