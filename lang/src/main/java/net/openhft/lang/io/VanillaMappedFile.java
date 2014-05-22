/*
 * Copyright 2014 Peter Lawrey
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/*
 * Merge memory mapped files:
 * - net.openhft.lang.io.MappedFile
 * - net.openhft.lang.io.MappedStore
 * - net.openhft.chronicle.VanillaFile
 */
public class VanillaMappedFile implements VanillaMappedResource {

    private final File path;
    private final FileChannel channel;
    private final VanillaMappedMode mode;
    private final long size;

    public VanillaMappedFile(final File path, VanillaMappedMode mode) throws IOException {
        this(path, mode, -1);
    }

    public VanillaMappedFile(final File path, VanillaMappedMode mode, long size) throws IOException {
        this.path = path;
        this.mode = mode;
        this.size = size;
        this.channel = fileChannel(path,mode,this.size);
    }

    public VanillaMappedBytes bytes(long address, long size) throws IOException {
        return bytes(address, size, -1);
    }

    public synchronized VanillaMappedBytes bytes(long address, long size, long index) throws IOException {
        MappedByteBuffer buffer = this.channel.map(this.mode.mapValue(),address,size);
        buffer.order(ByteOrder.nativeOrder());

        return new VanillaMappedBytes(buffer,index);
    }

    @Override
    public String path() {
        return this.path.getAbsolutePath();
    }

    @Override
    public long size() {
        try {
            return this.channel.size();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if(this.channel.isOpen()) {
            this.channel.close();
        }
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    private static FileChannel fileChannel(final File path, VanillaMappedMode mapMode, long size) throws IOException {
        FileChannel fileChannel = null;
        try {
            final RandomAccessFile raf = new RandomAccessFile(path, mapMode.stringValue());
            if (size > 0 && raf.length() != size) {
                if (mapMode.mapValue() != FileChannel.MapMode.READ_WRITE) {
                    throw new IOException("Cannot resize file to " + size + " as mode is not READ_WRITE");
                }

                raf.setLength(size);
            }

            fileChannel = raf.getChannel();
            fileChannel.force(true);
        } catch (Exception e) {
            throw wrap(e);
        }

        return fileChannel;
    }

    private static IOException wrap(Throwable throwable) {
        if(throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        } else if(throwable instanceof IOException) {
            return (IOException)throwable;
        }

        return new IOException(throwable);
    }

    public static VanillaMappedFile readWrite(final File path) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RW);
    }

    public static VanillaMappedFile readWrite(final File path, long size) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RW,size);
    }

    public static VanillaMappedFile readOnly(final File path) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RO);
    }

    public static VanillaMappedFile readOnly(final File path, long size) throws IOException {
        return new VanillaMappedFile(path,VanillaMappedMode.RO,size);
    }
}
