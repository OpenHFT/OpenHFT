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
package net.openhft.lang.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VanillaMappedBlocks implements VanillaMappedResource {
    private final VanillaMappedFile mappedFile;
    private final List<VanillaMappedBytes> bytes;
    private final long blockSize;

    private VanillaMappedBytes mb0;
    private VanillaMappedBytes mb1;

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize, long overlapSize) throws IOException {
        this(path,mode,blockSize + overlapSize);
    }

    public VanillaMappedBlocks(final File path, VanillaMappedMode mode, long blockSize) throws IOException {
        this.mappedFile = new VanillaMappedFile(path,mode,-1);
        this.bytes = new ArrayList<VanillaMappedBytes>();
        this.blockSize = blockSize;
        this.mb0 = null;
        this.mb1 = null;
    }

    public synchronized VanillaMappedBytes acquire(long index) throws IOException {
        if(this.mb0 != null && this.mb0.index() == index) {
            this.mb0.reserve();
            return this.mb0;
        }

        if(this.mb1 != null && this.mb1.index() == index) {
            this.mb1.reserve();
            return this.mb1;
        }

        return acquire0(index);
    }

    protected VanillaMappedBytes acquire0(long index) throws IOException {

        if(this.mb1 != null) {
            this.mb1.release();
        }

        this.mb1 = this.mb0;
        this.mb0 = this.mappedFile.bytes(index * this.blockSize, this.blockSize, index);
        this.mb0.reserve();;

        bytes.add(this.mb0);

        for (int i = bytes.size() - 1; i >= 0; i--) {
            if(bytes.get(i).unmapped()) {
                bytes.remove(i);
            }
        }

        return this.mb0;
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
        if(this.mb0 != null && !this.mb0.unmapped()) {
            this.mb0.release();
            this.mb0 = null;
        }

        if(this.mb1 != null && !this.mb1.unmapped()) {
            this.mb1.release();
            this.mb1 = null;
        }

        //int count = 0;

        for (VanillaMappedBytes vmb : this.bytes) {
            //if (!vmb.unmapped()) {
            //    vmb.release();
                //if (!vmb.unmapped()) {
                //    count++;
                //}
            //}

            vmb.cleanup();
        }

        //if(count > 0) {
        //    LOG.info(this.mappedFile.path() + ": memory mappings left unmapped, num= " + count);
        //}

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
