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
import java.util.LinkedList;
import java.util.List;

/*
 * Merge memory mapped files:
 * - net.openhft.lang.io.MappedFile
 * - net.openhft.lang.io.MappedStore
 * - net.openhft.chronicle.sandbox.VanillaFile
 */
public class VanillaMappedFile {

    private final FileChannel channel;
    private final VanillaMappedMode mode;
    private final long size;
    private long address;
    private List<VanillaMappedBlocks> blocks;

    public VanillaMappedFile(final File path, VanillaMappedMode mode) throws IOException {
        this(path,mode,-1);
    }

    public VanillaMappedFile(final File path, VanillaMappedMode mode, long size) throws IOException {
        this.mode = mode;
        this.size = size;
        this.address = 0;
        this.channel = fileChannel(path,mode,this.size);
        this.blocks = new LinkedList<VanillaMappedBlocks>();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public VanillaMappedBuffer sliceOf(long size) throws IOException {
        return sliceAtWithId(this.address, size, -1);
    }

    public synchronized VanillaMappedBuffer sliceOfWithId(long size, long id) throws IOException {
        return sliceAtWithId(this.address, size, id);
    }

    public synchronized VanillaMappedBuffer sliceAt(long address, long size) throws IOException {
        return sliceAtWithId(address, size, -1);
    }

    public synchronized VanillaMappedBuffer sliceAtWithId(long address, long size, long id) throws IOException {
        MappedByteBuffer buffer = this.channel.map(this.mode.mapValue(),address,size);
        buffer.order(ByteOrder.nativeOrder());

        if(address + size > this.address) {
            this.address = address + size;
        }

        return new VanillaMappedBuffer(buffer,id);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public VanillaMappedBlocks blocks(final long blockSize, final long overlapSize) throws IOException {
        return blocks(blockSize + overlapSize);
    }

    public VanillaMappedBlocks blocks(final long size) throws IOException {
        final List<VanillaMappedBuffer> buffers = new LinkedList<VanillaMappedBuffer>();
        final VanillaMappedBlocks vmb = new VanillaMappedBlocks() {
            @Override
            public VanillaMappedBuffer acquire(long index) throws IOException {
                VanillaMappedBuffer mb = null;

                for(int i = buffers.size() - 1; i >= 0;i--) {
                    if(buffers.get(i).id() == index && !buffers.get(i).unmapped()) {
                        // if mapped, get id and increase usage
                        mb = buffers.get(i);
                        mb.reserve();
                    } else if(buffers.get(i).refCount() <= 0) {
                        // if unmapped and not used (reference count <= 0) unmap
                        // it and clean id up
                        if(!buffers.get(i).unmapped()) {
                            buffers.get(i).cleanup();
                        }

                        buffers.remove(i);
                    }
                }

                if(mb == null) {
                    mb = VanillaMappedFile.this.sliceAtWithId(index * size,size,index);
                    buffers.add(mb);
                }

                return mb;
            }

            @Override
            public void close() throws IOException {
                for(int i = buffers.size() - 1; i >= 0;i--) {
                    buffers.get(i).cleanup();
                }
            }
        };

        this.blocks.add(vmb);

        return vmb;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public long size() throws IOException {
        return this.channel.size();
    }

    public synchronized void close() throws IOException {
        for(VanillaMappedBlocks vmb : this.blocks) {
            vmb.close();
        }

        this.blocks.clear();
        this.channel.close();
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
}
