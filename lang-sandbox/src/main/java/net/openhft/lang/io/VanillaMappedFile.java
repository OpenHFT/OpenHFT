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
import java.nio.channels.FileChannel;

/*
 * Merge memory mapped files:
 * - net.openhft.lang.io.MappedFile
 * - net.openhft.lang.io.MappedStore
 * - net.openhft.chronicle.sandbox.VanillaFile
 */
public class VanillaMappedFile {

    private final FileChannel fileChannel;
    private final VanillaMappedMode mode;
    private final long size;
    private final long blockSize;
    private final long overlapSize;

    public VanillaMappedFile(final File path, VanillaMappedMode mode, long size) throws IOException {
        this.mode = mode;
        this.size = size;
        this.blockSize = -1;
        this.overlapSize = -1;
        this.fileChannel = fileChannel(path,mode,this.size);

        //this.address = map0(fileChannel, imodeFor(mode), 0L, size);
        //this.cleaner = Cleaner.create(this, new Unmapper(address, size, fileChannel));
    }

    public VanillaMappedFile(final File path, VanillaMappedMode mode, long blockSize, long overlapSize) throws IOException {
        this.mode = mode;
        this.size = -1;
        this.blockSize = blockSize;
        this.overlapSize =overlapSize;
        this.fileChannel = fileChannel(path,mode,this.size);

        //this.address = map0(fileChannel, imodeFor(mode), 0L, size);
        //this.cleaner = Cleaner.create(this, new Unmapper(address, size, fileChannel));
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    private static FileChannel fileChannel(final File path, VanillaMappedMode mapMode, long size) throws IOException {
        FileChannel fileChannel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(path, mapMode.stringValue());
            if (size > 0 && raf.length() != size) {
                if (mapMode.mapValue() != FileChannel.MapMode.READ_WRITE) {
                    throw new IOException("Cannot resize file to " + size + " as mode is not READ_WRITE");
                }

                raf.setLength(size);
            }

            fileChannel = raf.getChannel();
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
}
