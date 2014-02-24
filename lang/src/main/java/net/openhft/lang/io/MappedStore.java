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
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedStore implements BytesStore {

    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
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
        if (size < 0) throw new IllegalArgumentException("size: " + size);
        this.size = size;
        this.bytesMarshallerFactory = bytesMarshallerFactory;
        fileChannel = new RandomAccessFile(file, mode == FileChannel.MapMode.READ_WRITE ? "rw" : "r").getChannel();
        try {
            FileDescriptor fd = getFD(fileChannel);
            if (fileChannel.size() < size) {
                if (mode != FileChannel.MapMode.READ_WRITE)
                    throw new IOException("Cannot resize file to " + size + " as mode is not READ_WRITE");
                int rv;
                do {
                    rv = truncate0(fd, size);
                } while ((rv == IOStatus.INTERRUPTED) && fileChannel.isOpen());
            }
            int imode = imodeFor(mode);
            address = map0(fileChannel, imode, 0L, size);
            cleaner = Cleaner.create(this, new Unmapper(address, size, fd));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private static long map0(FileChannel fileChannel, int imode, long start, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method map0 = fileChannel.getClass().getDeclaredMethod("map0", int.class, long.class, long.class);
        map0.setAccessible(true);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }

    private static int truncate0(FileDescriptor fd, long size) throws IOException {
        try {
            Method close0 = Class.forName("sun.nio.ch.FileDispatcherImpl").getDeclaredMethod("truncate0", FileDescriptor.class, long.class);
            close0.setAccessible(true);
            return (Integer) close0.invoke(null, fd, size);
        } catch (Exception e) {
            throw wrap(e);
        }
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

    private static void close0(FileDescriptor fd) {
        try {
            Method close0 = Class.forName("sun.nio.ch.FileDispatcherImpl").getDeclaredMethod("close0", FileDescriptor.class);
            close0.setAccessible(true);
            close0.invoke(null, fd);
        } catch (Exception ignored) {
        }
    }

    private static IOException wrap(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
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
    public DirectBytes createSlice() {
        return new DirectBytes(this, refCount);
    }

    @NotNull
    public DirectBytes createSlice(long offset, long length) {
        return new DirectBytes(this, refCount, offset, length);
    }

    private int imodeFor(FileChannel.MapMode mode) {
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

    private FileDescriptor getFD(FileChannel fileChannel) throws IOException {
        try {
            Field fd = fileChannel.getClass().getDeclaredField("fd");
            fd.setAccessible(true);
            return (FileDescriptor) fd.get(fileChannel);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    enum IOStatus {
        ;

        static final int EOF = -1;
        static final int UNAVAILABLE = -2;
        static final int INTERRUPTED = -3;
        static final int UNSUPPORTED = -4;
        static final int THROWN = -5;
        static final int UNSUPPORTED_CASE = -6;
    }

    static class Unmapper implements Runnable {
        private final long size;
        private final FileDescriptor fd;
        private volatile long address;

        Unmapper(long address, long size, FileDescriptor fd) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.fd = fd;
        }

        public void run() {
            if (address == 0)
                return;
            try {
                unmap0(address, size);
                address = 0;

                // if this mapping has a valid file descriptor then we close it
                if (fd.valid()) {
                    close0(fd);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
