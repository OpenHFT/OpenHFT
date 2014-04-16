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
import java.util.ArrayList;
import java.util.List;

public class VanillaMappedBlocks implements VanillaMappedResource {
    private final VanillaMappedFile mappedFile;
    private final List<VanillaMappedBuffer> buffers;
    private final long blockSize;
    private final Object lock;

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize, long overlapSize) throws IOException {
        this(path,mode,blockSize + overlapSize);
    }

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize) throws IOException {
        this.mappedFile = new VanillaMappedFile(path,mode,-1);
        this.buffers    = new ArrayList<VanillaMappedBuffer>();
        this.blockSize  = blockSize;
        this.lock       = new Object();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public VanillaMappedBuffer acquire(long index) throws IOException {
        VanillaMappedBuffer mb = null;

        synchronized(this.lock) {
            for (int i = buffers.size() - 1; i >= 0; i--) {
                if (buffers.get(i).index() == index && !buffers.get(i).unmapped()) {
                    // if mapped, get id and increase usage
                    mb = buffers.get(i);
                    mb.reserve();
                } else if (buffers.get(i).refCount() <= 0 || buffers.get(i).unmapped()) {
                    // if not unmapped and not used (reference count <= 0)
                    // unmap it and clean id up
                    if (!buffers.get(i).unmapped()) {
                        buffers.get(i).cleanup();
                    }

                    buffers.remove(i);
                }
            }

            if (mb == null) {
                mb = this.mappedFile.sliceAt(index * this.blockSize, this.blockSize, index);
                buffers.add(mb);
            }
        }

        return mb;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public String path() {
        return this.mappedFile.path();
    }

    @Override
    public long size() {
        return this.mappedFile.size();
    }

    @Override
    public void close() throws IOException {
        //TODO: resource leack check
        //int count = 0;

        synchronized(this.lock) {
            for (VanillaMappedBuffer vmb : this.buffers) {
                //if (vmb.refCount() > 0) {
                //    count++;
                //}

                vmb.cleanup();
            }

            /*
            if(count > 0) {
                Logger.getLogger(VanillaMappedBlocks.class.getName()).info(
                    this.mappedFile.path()
                        + ": memory mappings left unreleased, num= " + count
                );
            }
            */

            this.buffers.clear();
            this.mappedFile.close();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static VanillaMappedBlocks readWrite(final File path, long size) throws IOException {
        return new VanillaMappedBlocks(path,VanillaMappedMode.RW,size);
    }

    public static VanillaMappedBlocks readOnly(final File path, long size) throws IOException {
        return new VanillaMappedBlocks(path,VanillaMappedMode.RO,size);
    }
}
