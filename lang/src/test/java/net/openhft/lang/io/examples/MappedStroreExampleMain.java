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

package net.openhft.lang.io.examples;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.MappedStore;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by peter.lawrey on 14/07/14.
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
