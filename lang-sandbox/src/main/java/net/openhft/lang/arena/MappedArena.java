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

import net.openhft.lang.io.*;
import net.openhft.lang.io.serialization.ObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by peter.lawrey on 22/06/14.
 */
public class MappedArena implements Arena {
    static final byte[] MAGIC = "Arena001".getBytes();
    static final int MAGIC_OFFSET = 0;
    static final int LOCK_OFFSET = MAGIC_OFFSET + 8;
    static final int ALLOCATE_SIZE_OFFSET = LOCK_OFFSET + 8;
    static final int ALLOCATIONS_OFFSET = ALLOCATE_SIZE_OFFSET + 4;
    static final int HEADER_ID_OFFSET0 = 32;
    static final int HEADER_ID_SIZE = 4;
    static final int HEADER_LENGTH = 64;

    final MappedArenaStores stores;
    private final Mode mode;
    private final OffHeapLock readLock;
    private final OffHeapLock writeLock;
    private final DirectBytes header;

    /**
     * The lock consists of the reading/writing mode, writer count, writers waiting, reader count, readers waiting
     */
    public MappedArena(File file, long minSize, ObjectSerializer objectSerializer, Mode mode) throws IOException {
        this.mode = mode;
        stores = new MappedArenaStores(file, FileChannel.MapMode.READ_WRITE, minSize, objectSerializer);
        header = stores.acquire(0, HEADER_LENGTH);
        OffHeapReadWriteLock ohrwl = new OffHeapReadWriteLock(header, LOCK_OFFSET);
        readLock = ohrwl.readLock();
        writeLock = ohrwl.writeLock();
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public OffHeapLock readLock() {
        return readLock;
    }

    @Override
    public OffHeapLock writeLock() {
        return writeLock;
    }

    @Override
    public int blockSizeBits() {
        return 0;
    }

    @Override
    public int allocate(int hash, int sizeInBlocks) {
        return 0;
    }

    @Override
    public int reallocate(int hash, int handle, int sizeInBlocks) {
        return 0;
    }

    @Override
    public void free(int hash, int handle, int sizeInBlocks) {
    }

    @Override
    public void copyTo(Bytes bytes, int handle) {
    }

    @Override
    public void copyFrom(int handle, Bytes bytes) {
    }

    @Override
    public void setBytes(int handle, NativeBytes bytes) throws IllegalStateException {
    }

    @Override
    public int firstHandleFor(int hash) {
        return 0;
    }

    @Override
    public int nextHandle() {
        return 0;
    }

    @Override
    public int handlesUsed() {
        return 0;
    }

    @Override
    public int handlesCapacity() {
        return 0;
    }

    @Override
    public int entriesUsed() {
        return 0;
    }

    @Override
    public int entriesCapacity() {
        return 0;
    }
}
