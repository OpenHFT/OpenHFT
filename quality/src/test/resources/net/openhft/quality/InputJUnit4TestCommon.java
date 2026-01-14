package net.openhft.quality;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class InputJUnit4TestCommon {
    @Test
    public void helperTest() {
        assertTrue("helper should remain stable during retries because retries re-use state", true);
    }
}
