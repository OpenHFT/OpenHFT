/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    @Test
    public void testSample()   {
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

    @Test
    public void testVanillaDiff() {
        VanillaDifferencer vd = new VanillaDifferencer();
        assertEquals(100, vd.sample(123400, 123500));
    }
}
