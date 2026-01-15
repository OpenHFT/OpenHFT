/*
 * Test input with low-entropy messages for the Shannon entropy rule.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputLowEntropyMessages {
    public void test() {
        assertEquals(1, 1, "cache entry cache entry cache entry stays valid because recovery replays");
    }
}
