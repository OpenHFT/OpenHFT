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

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.OffHeapLock;

/**
 * Created by peter.lawrey on 20/06/14.
 */
public interface Arena {
    enum Mode {
        SINGLE_BLOCK, CONTINUOUS_BLOCKS, LINKED_BLOCKS
    }

    Mode getMode();

    OffHeapLock readLock();

    OffHeapLock writeLock();

    // log2(allocation multiple)
    int blockSizeBits();

    // allocate a block of memory
    int /* handle */ allocate(int hash, int sizeInBlocks);

    // resize a block of memory
    int /* new handle */ reallocate(int hash, int handle, int sizeInBlocks);

    // free a block of memory
    void free(int hash, int handle, int sizeInBlocks);

    // read to data stored.
    void copyTo(Bytes bytes, int handle);

    // read from data stored
    void copyFrom(int handle, Bytes bytes);

    // map the data directly.
    void setBytes(int handle, NativeBytes bytes) throws IllegalStateException;

    // start a hash lookup
    int firstHandleFor(int hash);

    // next hash or 0
    int nextHandle();

    // metrics
    int handlesUsed();

    int handlesCapacity();

    int entriesUsed();

    int entriesCapacity();
}
