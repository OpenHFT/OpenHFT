/*
 * Test input for duplicate assert messages check.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

public class InputDuplicateAssertMessages {

    public void testDuplicates() {
        // First occurrence - OK
        assertEquals("expected checksum should match", 1, 1);

        // Some other assertion - OK
        assertEquals("this scenario should pass", 2, 2);

        // Duplicate - should warn
        assertEquals("expected checksum should match", 3, 3);
    }
}
