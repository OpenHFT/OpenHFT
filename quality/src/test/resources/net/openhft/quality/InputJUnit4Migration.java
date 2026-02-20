package net.openhft.quality;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputJUnit4Migration {
    @Before
    public void setUp() {
        assertTrue("cache should remain warm after init because cold starts are noisy", true);
    }

    @Test
    public void shouldComputeTotal() {
        assertEquals("total should remain stable after update", 1, 1);
    }

    @After
    public void tearDown() {
        assertTrue("resources should close cleanly after test", true);
    }
}
