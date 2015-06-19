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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * This manages the full life cycle of a file and its mappings.
 */
public class MappedFile {
    private final String filename;
    private final long blockSize;
    private final long overlapSize;

    private final FileChannel fileChannel;
    private final List<MappedMemory> maps = new ArrayList<MappedMemory>();
    // short list of the last two mappings.
    private volatile MappedMemory map0, map1;

    public MappedFile(String filename, long blockSize) throws FileNotFoundException {
        this(filename, blockSize, 0L);
    }

    public MappedFile(String filename, long blockSize, long overlapSize) throws FileNotFoundException {
        this.filename = filename;
        this.blockSize = blockSize;
        this.overlapSize = overlapSize;
        fileChannel = new RandomAccessFile(filename, "rw").getChannel();
    }

    public String name() {
        return filename;
    }

    public static MappedByteBuffer getMap(@NotNull FileChannel fileChannel, long start, int size) throws IOException {
        for (int i = 1; ; i++) {
            try {
//                long startTime = System.nanoTime();
                @SuppressWarnings("UnnecessaryLocalVariable")
                MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, start, size);
                map.order(ByteOrder.nativeOrder());
//                long time = System.nanoTime() - startTime;
//                System.out.printf("Took %,d us to map %,d MB%n", time / 1000, size / 1024 / 1024);
//                System.out.println("Map size: "+size);
                return map;
            } catch (IOException e) {
                if (e.getMessage() == null || !e.getMessage().endsWith("user-mapped section open")) {
                    throw e;
                }
                if (i < 10)
                    //noinspection CallToThreadYield
                    Thread.yield();
                else
                    try {
                        //noinspection BusyWait
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
            }
        }
    }

    public MappedMemory acquire(long index) throws IOException {
        MappedMemory map0 = this.map0, map1 = this.map1;
        if (map0 != null && map0.index() == index) {
            map0.reserve();
            return map0;
        }
        if (map1 != null && map1.index() == index) {
            map1.reserve();
            return map1;
        }
        return acquire0(index);
    }

    /**
     * gets the refCount a given index, or returns 0, if the there is no mapping for this index
     *
     * @param index
     * @return the mapping at this {@code index}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public int getRefCount(long index) throws IOException {
        try {
            for (MappedMemory m : maps) {
                if (m.index() == index)
                    return m.refCount();
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    private synchronized MappedMemory acquire0(long index) throws IOException {
        if (map1 != null)
            map1.release();
        map1 = map0;
        map0 = new MappedMemory(fileChannel.map(FileChannel.MapMode.READ_WRITE, index * blockSize, blockSize + overlapSize), index);
        map0.reserve();
        maps.add(map0);
        // clean up duds.
        for (int i = maps.size() - 1; i >= 0; i--) {
            if (maps.get(i).refCount() <= 0)
                maps.remove(i);
        }
        return map0;
    }

    public synchronized void close() throws IOException {
        if (map1 != null) {
            map1.release();
            map1 = null;
        }
        if (map0 != null) {
            map0.release();
            map0 = null;
        }
        // clean up errant maps.
        int count = 0;
        for (int i = maps.size() - 1; i >= 0; i--) {
            if (maps.get(i).refCount() <= 0) {
                maps.get(i).close();
                count++;
            }
        }
        if (count > 1) {
            LoggerFactory.getLogger(MappedFile.class).info("{} memory mappings left unreleased, num= {}", filename, count);
        }

        maps.clear();
        fileChannel.close();
    }

    public long size() {
        try {
            return fileChannel.size();
        } catch (IOException e) {
            return 0;
        }
    }

    public long blockSize() {
        return blockSize;
    }

    public void release(MappedMemory mappedMemory) {
        if (mappedMemory.release()) {
            if (map0 == mappedMemory)
                map0 = null;
            if (map1 == mappedMemory)
                map1 = null;
        }
    }
}
