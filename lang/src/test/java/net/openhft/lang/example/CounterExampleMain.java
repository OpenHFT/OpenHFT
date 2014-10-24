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
