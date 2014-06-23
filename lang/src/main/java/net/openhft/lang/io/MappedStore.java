/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;
import net.openhft.lang.model.constraints.NotNull;
import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedStore implements BytesStore {

    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;

    // retain to prevent GC.
    private final File file;
    private final FileChannel fileChannel;
    private final Cleaner cleaner;
    private final long address;
    private final AtomicInteger refCount = new AtomicInteger(1);
    private final long size;
    private final BytesMarshallerFactory bytesMarshallerFactory;

    public MappedStore(File file, FileChannel.MapMode mode, long size) throws IOException {
        this(file, mode, size, new VanillaBytesMarshallerFactory());
    }

    public MappedStore(File file, FileChannel.MapMode mode, long size, BytesMarshallerFactory bytesMarshallerFactory) throws IOException {
        if (size < 0 || size > 128L << 40) {
            throw new IllegalArgumentException("invalid size: " + size);
        }

        this.file = file;
        this.size = size;
        this.bytesMarshallerFactory = bytesMarshallerFactory;

        try {
            RandomAccessFile raf = new RandomAccessFile(file, accesModeFor(mode));
            if (raf.length() != this.size) {
                if (mode != FileChannel.MapMode.READ_WRITE) {
                    throw new IOException("Cannot resize file to " + size + " as mode is not READ_WRITE");
                }

                raf.setLength(this.size);
            }

            this.fileChannel = raf.getChannel();
            this.address = map0(fileChannel, imodeFor(mode), 0L, size);
            this.cleaner = Cleaner.create(this, new Unmapper(address, size, fileChannel));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    @Override
    public BytesMarshallerFactory bytesMarshallerFactory() {
        return bytesMarshallerFactory;
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

    private static String accesModeFor(FileChannel.MapMode mode) {
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

    public File file() {
        return file;
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

