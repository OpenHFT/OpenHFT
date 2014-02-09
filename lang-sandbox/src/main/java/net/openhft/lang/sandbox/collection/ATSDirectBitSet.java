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
    public static final long NOT_FOUND = -1L;
    private final Bytes bytes;
    private final long longLength;

    public ATSDirectBitSet(Bytes bytes) {
        this.bytes = bytes;
        longLength = bytes.length() >> 8;
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
        long fromLongIndex = fromIndex >> 6;
        int bitIndex = (int) (fromIndex & 0x3f);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3) >>> bitIndex;
        if (l != 0) {
            return Long.numberOfTrailingZeros(l) + bitIndex;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = bytes.readLong(i << 3);
            if (l != 0)
                return (i << 6) + Long.numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long nextSetLong(long fromLongIndex) {
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3);
        if (l != 0)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = bytes.readLong(i << 3);
            if (l != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long nextClearBit(long fromIndex) {
        long fromLongIndex = fromIndex >> 6;
        int bitIndex = (int) (fromIndex & 0x3f);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = ~(bytes.readVolatileLong(fromLongIndex << 3) >>> bitIndex);
        if (l != 0) {
            return Long.numberOfTrailingZeros(l) + bitIndex;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = ~bytes.readLong(i << 3);
            if (l != 0)
                return (i << 6) + Long.numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long nextClearLong(long fromLongIndex) {
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3);
        if (l != ~0)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = bytes.readLong(i << 3);
            if (l != ~0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetBit(long fromIndex) {
        long fromLongIndex = fromIndex >> 6;
        int bitIndex = (int) (fromIndex & 0x3f);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3) << -bitIndex;
        if (l != 0)
            return fromLongIndex << 6 + Long.numberOfLeadingZeros(l) + bitIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = bytes.readLong(i << 3);
            if (l != 0)
                return fromLongIndex << 6 + Long.numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetLong(long fromLongIndex) {
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3);
        if (l != 0)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = bytes.readLong(i << 3);
            if (l != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousClearBit(long fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long previousClearLong(long fromLongIndex) {
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = ~bytes.readVolatileLong(fromLongIndex << 3);
        if (l != 0)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = ~bytes.readLong(i << 3);
            if (l != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long length() {
        return longLength << 6;
    }

    @Override
    public long cardinality() {
        long count = Long.bitCount(bytes.readVolatileLong(0));
        for (long i = 1; i < longLength; i++) {
            count += Long.bitCount(bytes.readLong(i << 3));
        }
        return count;
    }

    @Override
    public DirectBitSet and(long index, long value) {
        while (true) {
            long l = bytes.readVolatileLong(index << 3);
            long l2 = l & value;
            if (l == l2 || bytes.compareAndSwapLong(index << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet or(long index, long value) {
        while (true) {
            long l = bytes.readVolatileLong(index << 3);
            long l2 = l | value;
            if (l == l2 || bytes.compareAndSwapLong(index << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet xor(long index, long value) {
        while (true) {
            long l = bytes.readVolatileLong(index << 3);
            long l2 = l ^ value;
            if (bytes.compareAndSwapLong(index << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet andNot(long index, long value) {
        throw new UnsupportedOperationException();
    }
}
