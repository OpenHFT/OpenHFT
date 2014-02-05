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

package net.openhft.lang.sandbox.collection;

import net.openhft.lang.io.Bytes;

/**
 * DirectBitSet with input validations and ThreadSafe memory access.
 */
public class ATSDirectBitSet implements DirectBitSet {
    private final Bytes bytes;

    public ATSDirectBitSet(Bytes bytes) {
        this.bytes = bytes;
    }

    @Override
    public void reserve() {
        bytes.reserve();
    }

    @Override
    public void release() {
        bytes.release();
    }

    @Override
    public int refCount() {
        return bytes.refCount();
    }

    @Override
    public DirectBitSet flip(long bitIndex) {
        long index64 = bitIndex >> 6;
        int bit = (int) (bitIndex & 0x3F);
        if (bitIndex < 0 || index64 >= bytes.capacity())
            throw new IllegalArgumentException();
        long mask = (1L << bit);
        while (true) {
            long l = bytes.readVolatileLong(index64 << 3);
            long l2 = l ^ mask;
            if (bytes.compareAndSwapLong(index64 << 3, l, l2))
                return this;
        }
    }

    @Override
    public DirectBitSet flip(long fromIndex, long toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet set(long bitIndex) {
        long index64 = bitIndex >> 6;
        int bit = (int) (bitIndex & 0x3F);
        if (bitIndex < 0 || index64 >= bytes.capacity())
            throw new IllegalArgumentException();
        long mask = 1L << bit;
        while (true) {
            long l = bytes.readVolatileLong(index64 << 3);
            if ((l & mask) != 0) return this;
            long l2 = l | mask;
            if (bytes.compareAndSwapLong(index64 << 3, l, l2))
                return this;
        }
    }

    @Override
    public DirectBitSet set(long bitIndex, boolean value) {
        return value ? set(bitIndex) : clear(bitIndex);
    }

    @Override
    public DirectBitSet set(long fromIndex, long toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet set(long fromIndex, long toIndex, boolean value) {
        return value ? set(fromIndex, toIndex) : clear(fromIndex, toIndex);
    }

    @Override
    public DirectBitSet clear(long bitIndex) {
        long index64 = bitIndex >> 6;
        int bit = (int) (bitIndex & 0x3F);
        if (bitIndex < 0 || index64 >= bytes.capacity())
            throw new IllegalArgumentException();
        long mask = 1L << bit;
        while (true) {
            long l = bytes.readVolatileLong(index64 << 3);
            if ((l & mask) == 0) return this;
            long l2 = l & ~mask;
            if (bytes.compareAndSwapLong(index64 << 3, l, l2))
                return this;
        }
    }

    @Override
    public DirectBitSet clear(long fromIndex, long toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet clear() {
        bytes.clear();
        return this;
    }

    @Override
    public boolean get(long bitIndex) {
        long index64 = bitIndex >> 6;
        int bit = (int) (bitIndex & 0x3F);
        if (bitIndex < 0 || index64 >= bytes.capacity())
            throw new IllegalArgumentException();
        long l = bytes.readVolatileLong(index64 << 3);
        return (l >> bit) != 0;
    }

    @Override
    public long getLong(long index64) {
        if (index64 < 0 || index64 >= bytes.capacity())
            throw new IllegalArgumentException();
        return bytes.readVolatileLong(index64 << 3);
    }

    @Override
    public long nextSetBit(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long nextSetLong(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long nextClearBit(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long nextClearLong(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long previousSetBit(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long previousSetLong(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long previousClearBit(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long previousClearLong(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long length() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long cardinality() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet and(long index, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet or(long index, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet xor(long index, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectBitSet andNot(long index, long value) {
        throw new UnsupportedOperationException();
    }
}
