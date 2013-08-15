package net.openhft.lang.testing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: peter
 * Date: 05/08/13
 * Time: 19:14
 */
public class RunningMinimumTest {
    @Test
    public void testSample() throws Exception {
        for (int k = 0; k < 1000; k++) {
            for (long delta : new long[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                RunningMinimum rm = new RunningMinimum(50 * 1000);
                int j;
                for (j = 0; j < 50 * 1000000; j += 1000000) {
                    long startTime = System.nanoTime() + j;
                    long endTime = System.nanoTime() + j + delta + (long) (Math.pow(10 * 1000, Math.random()) * 1000);
                    rm.sample(startTime, endTime);
                }
                assertEquals("delta=" + delta, delta, rm.minimum(), 10 * 1000);
            }
        }
    }
}
