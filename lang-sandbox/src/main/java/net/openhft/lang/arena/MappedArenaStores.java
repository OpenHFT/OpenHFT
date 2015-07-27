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

package net.openhft.lang.arena;

import net.openhft.lang.io.BytesStore;
import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.model.constraints.NotNull;
import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedArenaStores implements Closeable {

    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;

    // retain to prevent GC.
    private final File file;
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;
    private final FileChannel.MapMode mode;
    private final List<MappedArenaStore> storeList = new ArrayList<MappedArenaStore>();
    private final ObjectSerializer objectSerializer;

    public MappedArenaStores(File file, FileChannel.MapMode mode, long minSize, ObjectSerializer objectSerializer) throws IOException {
        if (minSize < 0 || minSize > 128L << 40) {
            throw new IllegalArgumentException("invalid minSize: " + minSize);
        }

        this.file = file;
        this.objectSerializer = objectSerializer;

        try {
            randomAccessFile = new RandomAccessFile(file, accessModeFor(mode));
            this.mode = mode;
            this.fileChannel = randomAccessFile.getChannel();
            storeList.add(new MappedArenaStore(0, minSize));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public DirectBytes acquire(long offset, long size) throws IOException {
        MappedArenaStore mas = acquireMAS(offset, size);
        return mas.bytes(offset - mas.offset, size);
    }

    private MappedArenaStore acquireMAS(long offset, long size) throws IOException {
        long end = offset + size;
        for (MappedArenaStore store : storeList) {
            if (store.offset >= offset && store.end <= end)
                return store;
        }
        try {
            MappedArenaStore store = new MappedArenaStore(offset, size);
            storeList.add(store);
            return store;
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
        for (MappedArenaStore store : storeList) {
            store.free();
        }
    }

    class MappedArenaStore implements BytesStore {
        private final long address;
        private final Cleaner cleaner;
        private final AtomicInteger refCount = new AtomicInteger(1);
        final long offset, size, end;

        MappedArenaStore(long offset, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
            this.offset = offset;
            this.size = size;
            this.end = offset + size;
            if (randomAccessFile.length() < end) {
                if (mode != FileChannel.MapMode.READ_WRITE) {
                    throw new IOException("Cannot resize file to " + end + " as mode is not READ_WRITE");
                }

                randomAccessFile.setLength(end);
            }
            this.address = map0(fileChannel, imodeFor(mode), offset, size);
            this.cleaner = Cleaner.create(this, new Unmapper(address, size, fileChannel));
        }

        @Override
        public ObjectSerializer objectSerializer() {
            return objectSerializer;
        }

        @Override
        public long address() {
            return address;
        }

        @Override
        public long size() {
            return size;
        }

        @Override
        public void free() {
            cleaner.clean();
        }

        @NotNull
        public DirectBytes bytes() {
            return new DirectBytes(this, refCount);
        }

        @NotNull
        public DirectBytes bytes(long offset, long length) {
            return new DirectBytes(this, refCount, offset, length);
        }

        @Override
        public File file() {
            return file;
        }
    }

    private static long map0(FileChannel fileChannel, int imode, long start, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method map0 = fileChannel.getClass().getDeclaredMethod("map0", int.class, long.class, long.class);
        map0.setAccessible(true);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }

    private static void unmap0(long address, long size) throws IOException {
        try {
            Method unmap0 = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class, long.class);
            unmap0.setAccessible(true);
            unmap0.invoke(null, address, size);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private static IOException wrap(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
    }

    private static String accessModeFor(FileChannel.MapMode mode) {
        return mode == FileChannel.MapMode.READ_WRITE ? "rw" : "r";
    }

    private static int imodeFor(FileChannel.MapMode mode) {
        int imode = -1;
        if (mode == FileChannel.MapMode.READ_ONLY)
            imode = MAP_RO;
        else if (mode == FileChannel.MapMode.READ_WRITE)
            imode = MAP_RW;
        else if (mode == FileChannel.MapMode.PRIVATE)
            imode = MAP_PV;
        assert (imode >= 0);
        return imode;
    }

    static class Unmapper implements Runnable {
        private final long size;
        private final FileChannel channel;
        private volatile long address;

        Unmapper(long address, long size, FileChannel channel) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.channel = channel;
        }

        public void run() {
            if (address == 0)
                return;

            try {
                unmap0(address, size);
                address = 0;

                if (channel.isOpen()) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

