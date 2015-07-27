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

package net.openhft.lang.example;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.MappedStore;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class CounterExampleMain {
    static volatile long id;

    public static void main(String... ignored) throws IOException {
        int counters = 128;
        int repeats = 100000;

        File file = new File(System.getProperty("java.io.tmpdir") + "/counters");
        MappedStore ms = new MappedStore(file, FileChannel.MapMode.READ_WRITE, counters * 8);
        DirectBytes slice = ms.bytes();

        long start = System.nanoTime();
        for (int j = 0; j < repeats; j++) {
            for (int i = 0; i < counters; i++) {
                id = slice.addAtomicLong(i * 8, 1);
            }
        }
        long time = System.nanoTime() - start;
        System.out.printf("Took %.3f second to increment %,d counters, %,d times, last id=%,d%n",
                time / 1e9, counters, repeats, id);
        ms.free();
    }
}
