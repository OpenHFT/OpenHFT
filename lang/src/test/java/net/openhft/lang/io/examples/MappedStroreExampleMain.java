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

package net.openhft.lang.io.examples;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.MappedStore;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by peter on 14/07/14.
 */
public class MappedStroreExampleMain {
    public static void main(String[] args) throws IOException {
    File deleteme = File.createTempFile("deleteme", ".tmp");
    deleteme.deleteOnExit();
    // 4 GB of memory.
    long size = 4L << 30;
    long start = System.currentTimeMillis();
    MappedStore ms = new MappedStore(deleteme, FileChannel.MapMode.READ_WRITE, size);
    DirectBytes bytes = ms.bytes();
    for(long i = 0; i < size; i+= 4)
        bytes.writeLong(i);
    ms.free();
    long time = System.currentTimeMillis() - start;
    System.out.printf("Wrote %,d MB/s%n", size / 1000 / time);
    }
}
