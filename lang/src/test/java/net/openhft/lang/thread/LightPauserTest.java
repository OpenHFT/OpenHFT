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

package net.openhft.lang.thread;

import org.junit.Test;

/**
 * Created by peter.lawrey on 11/12/14.
 */
public class LightPauserTest {
    @Test
    public void testLightPauser() throws InterruptedException {
        final LightPauser pauser = new LightPauser(100 * 1000, 100 * 1000);
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    pauser.pause();
            }
        };
        thread.start();

        for (int t = 0; t < 3; t++) {
            long start = System.nanoTime();
            int runs = 10000000;
            for (int i = 0; i < runs; i++)
                pauser.unpause();
            long time = System.nanoTime() - start;
            System.out.printf("Average time to unpark was %,d ns%n", time / runs);
            Thread.sleep(20);
        }
        thread.interrupt();
    }
}
