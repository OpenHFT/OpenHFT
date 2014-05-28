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
    private final List<VanillaMappedBytes> bytes;
    private final long blockSize;

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize, long overlapSize) throws IOException {
        this(path,mode,blockSize + overlapSize);
    }

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize) throws IOException {
        this.mappedFile = new VanillaMappedFile(path,mode,-1);
        this.bytes = new ArrayList<VanillaMappedBytes>();
        this.blockSize = blockSize;
    }

    public synchronized VanillaMappedBytes acquire(long index) throws IOException {
        VanillaMappedBytes mb = null;

        for (int i = bytes.size() - 1; i >= 0; i--) {
            if (bytes.get(i).index() == index && !bytes.get(i).unmapped()) {
                // if mapped, get it and increase usage
                mb = bytes.get(i);
                mb.reserve();
            } else if (bytes.get(i).refCount() <= 0 || bytes.get(i).unmapped()) {
                // if not unmapped and not used (reference count <= 0)
                // unmap it and clean id up
                if (!bytes.get(i).unmapped()) {
                    bytes.get(i).cleanup();
                }

                bytes.remove(i);
            }
        }

        if (mb == null) {
            mb = this.mappedFile.bytes(index * this.blockSize, this.blockSize, index);
            bytes.add(mb);
        }

        return mb;
    }

    public synchronized int blocks() {
        return this.bytes.size();
    }

    @Override
    public String path() {
        return this.mappedFile.path();
    }

    @Override
    public synchronized long size() {
        return this.mappedFile.size();
    }

    @Override
    public synchronized void close() throws IOException {
        //TODO: resource leack check
        //int count = 0;

        for (VanillaMappedBytes vmb : this.bytes) {
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

        this.bytes.clear();
        this.mappedFile.close();
    }

    public static VanillaMappedBlocks readWrite(final File path, long size) throws IOException {
        return new VanillaMappedBlocks(path,VanillaMappedMode.RW,size);
    }

    public static VanillaMappedBlocks readOnly(final File path, long size) throws IOException {
        return new VanillaMappedBlocks(path,VanillaMappedMode.RO,size);
    }
}
