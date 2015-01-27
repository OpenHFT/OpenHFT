/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.testing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
 * Date: 05/08/13
 * Time: 19:14
 */
public class RunningMinimumTest {
    @org.junit.Ignore("TC")
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
                assertEquals("delta=" + delta, delta, rm.minimum(), 40 * 1000);
            }
        }
    }

    @org.junit.Ignore("TC")
    @Test
    public void testVanillaDiff() {
        VanillaDifferencer vd = new VanillaDifferencer();
        assertEquals(100, vd.sample(123400, 123500));
    }
}
