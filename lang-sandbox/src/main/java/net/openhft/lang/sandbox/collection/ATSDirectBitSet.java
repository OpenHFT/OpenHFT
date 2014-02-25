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
    private final long longLength;

    public ATSDirectBitSet(Bytes bytes) {
        this.bytes = bytes;
        longLength = bytes.capacity() >> 3;
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
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        // only 6 lowest-order bits used, JLS 15.19
        long mask = 1L << bitIndex;
        while (true) {
            long l = bytes.readVolatileLong(byteIndex);
            long l2 = l ^ mask;
            if (bytes.compareAndSwapLong(byteIndex, l, l2))
                return this;
        }
    }

    @Override
    public DirectBitSet flip(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = fromIndex >> 6;
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = toIndex >> 6;
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 0x3F) != 0) {
                long fromByteIndex = fromLongIndex << 3;
                long mask = (~0L) << fromIndex;
                while (true) {
                    long l = bytes.readVolatileLong(fromByteIndex);
                    long l2 = l ^ mask;
                    if (bytes.compareAndSwapLong(fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 0x3F) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    while (true) {
                        long l = bytes.readVolatileLong(i << 3);
                        long l2 = ~l;
                        if (bytes.compareAndSwapLong(i << 3, l, l2))
                            break;
                    }
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    while (true) {
                        long l = bytes.readVolatileLong(i << 3);
                        long l2 = ~l;
                        if (bytes.compareAndSwapLong(i << 3, l, l2))
                            break;
                    }
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 0x3F))
                long mask = (~0L) >>> ~toIndex;
                while (true) {
                    long l = bytes.readVolatileLong(toByteIndex);
                    long l2 = l ^ mask;
                    if (bytes.compareAndSwapLong(toByteIndex, l, l2))
                        return this;
                }
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            while (true) {
                long l = bytes.readVolatileLong(byteIndex);
                long l2 = l ^ mask;
                if (bytes.compareAndSwapLong(byteIndex, l, l2))
                    return this;
            }
        }
        return this;
    }

    @Override
    public DirectBitSet set(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        while (true) {
            long l = bytes.readVolatileLong(byteIndex);
            if ((l & mask) != 0) return this;
            long l2 = l | mask;
            if (bytes.compareAndSwapLong(byteIndex, l, l2))
                return this;
        }
    }

    @Override
    public boolean setIfClear(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        while (true) {
            long l = bytes.readVolatileLong(byteIndex);
            long l2 = l | mask;
            if (l == l2)
                return false;
            if (bytes.compareAndSwapLong(byteIndex, l, l2))
                return true;
        }
    }


    @Override
    public DirectBitSet set(long bitIndex, boolean value) {
        return value ? set(bitIndex) : clear(bitIndex);
    }

    @Override
    public DirectBitSet set(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = fromIndex >> 6;
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = toIndex >> 6;
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 0x3F) != 0) {
                long fromByteIndex = fromLongIndex << 3;
                long mask = (~0L) << fromIndex;
                while (true) {
                    long l = bytes.readVolatileLong(fromByteIndex);
                    long l2 = l | mask;
                    if (bytes.compareAndSwapLong(fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 0x3F) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~0L);
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 0x3F))
                long mask = (~0L) >>> ~toIndex;
                while (true) {
                    long l = bytes.readVolatileLong(toByteIndex);
                    long l2 = l | mask;
                    if (bytes.compareAndSwapLong(toByteIndex, l, l2))
                        return this;
                }
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            while (true) {
                long l = bytes.readVolatileLong(byteIndex);
                long l2 = l | mask;
                if (bytes.compareAndSwapLong(byteIndex, l, l2))
                    return this;
            }
        }
        return this;
    }

    @Override
    public DirectBitSet setAll() {
        for (long i = 0; i < longLength; i++) {
            bytes.writeLong(i << 3, ~0L);
        }
        return this;
    }

    @Override
    public DirectBitSet set(long fromIndex, long toIndex, boolean value) {
        return value ? set(fromIndex, toIndex) : clear(fromIndex, toIndex);
    }

    @Override
    public DirectBitSet clear(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        while (true) {
            long l = bytes.readVolatileLong(byteIndex);
            if ((l & mask) == 0) return this;
            long l2 = l & ~mask;
            if (bytes.compareAndSwapLong(byteIndex, l, l2))
                return this;
        }
    }

    @Override
    public DirectBitSet clear(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = fromIndex >> 6;
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = toIndex >> 6;
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 0x3F) != 0) {
                long fromByteIndex = fromLongIndex << 3;
                long mask = ~((~0L) << fromIndex);
                while (true) {
                    long l = bytes.readVolatileLong(fromByteIndex);
                    long l2 = l & mask;
                    if (bytes.compareAndSwapLong(fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 0x3F) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    bytes.writeLong(i << 3, 0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    bytes.writeLong(i << 3, 0L);
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 0x3F))
                long mask = ~((~0L) >>> ~toIndex);
                while (true) {
                    long l = bytes.readVolatileLong(toByteIndex);
                    long l2 = l & mask;
                    if (bytes.compareAndSwapLong(toByteIndex, l, l2))
                        return this;
                }
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = (~((~0L) << fromIndex)) | (~((~0L) >>> ~toIndex));
            while (true) {
                long l = bytes.readVolatileLong(byteIndex);
                long l2 = l & mask;
                if (bytes.compareAndSwapLong(byteIndex, l, l2))
                    return this;
            }
        }
        return this;
    }

    @Override
    public DirectBitSet clear() {
        bytes.zeroOut();
        return this;
    }

    @Override
    public boolean get(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long l = bytes.readVolatileLong(longIndex << 3);
        return (l & (1L << bitIndex)) != 0;
    }

    @Override
    public long getLong(long longIndex) {
        if (longIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        return bytes.readVolatileLong(longIndex << 3);
    }

    @Override
    public long nextSetBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readVolatileLong(fromLongIndex << 3) >>> fromIndex;
        if (l != 0) {
            return fromIndex + Long.numberOfTrailingZeros(l);
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
        if (fromLongIndex < 0)
            throw new IndexOutOfBoundsException();
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        if (bytes.readVolatileLong(fromLongIndex << 3) != 0)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            if (bytes.readLong(i << 3) != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long nextClearBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = (~bytes.readVolatileLong(fromLongIndex << 3)) >>> fromIndex;
        if (l != 0) {
            return fromIndex + Long.numberOfTrailingZeros(l);
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = ~bytes.readLong(i << 3);
            if (l != 0)
                return (i << 6) + Long.numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long setOne(long fromIndex) {
        while (true) {
            long fromLongIndex = fromIndex >> 6;
            if (fromLongIndex >= longLength)
                return NOT_FOUND;
            fromIndex = nextClearBit(fromIndex);
            if (fromIndex == NOT_FOUND)
                return NOT_FOUND;
            if (setIfClear(fromIndex))
                return fromIndex;
        }
    }

    @Override
    public long nextClearLong(long fromLongIndex) {
        if (fromLongIndex < 0)
            throw new IndexOutOfBoundsException();
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        if (bytes.readVolatileLong(fromLongIndex << 3) != ~0L)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            if (bytes.readLong(i << 3) != ~0L)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetBit(long fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength) {
            // the same policy for this "index out of bounds" situation
            // as in j.u.BitSet
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        // << ~fromIndex === << (63 - (fromIndex & 0x3F))
        long l = bytes.readVolatileLong(fromLongIndex << 3) << ~fromIndex;
        if (l != 0)
            return fromIndex - Long.numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = bytes.readLong(i << 3);
            if (l != 0)
                return (i << 6) + 63 - Long.numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetLong(long fromLongIndex) {
        if (fromLongIndex < 0) {
            if (fromLongIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }
        if (fromLongIndex >= longLength)
            fromLongIndex = longLength - 1;
        if (bytes.readVolatileLong(fromLongIndex << 3) != 0)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            if (bytes.readLong(i << 3) != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousClearBit(long fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        long l = (~bytes.readVolatileLong(fromLongIndex << 3)) << ~fromIndex;
        if (l != 0)
            return fromIndex - Long.numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = ~bytes.readLong(i << 3);
            if (l != 0)
                return (i << 6) + 63 - Long.numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long previousClearLong(long fromLongIndex) {
        if (fromLongIndex < 0) {
            if (fromLongIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }
        if (fromLongIndex >= longLength)
            fromLongIndex = longLength - 1;
        if (bytes.readVolatileLong(fromLongIndex << 3) != ~0L)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            if (bytes.readLong(i << 3) != ~0L)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long size() {
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
    public DirectBitSet and(long longIndex, long value) {
        while (true) {
            long l = bytes.readVolatileLong(longIndex << 3);
            long l2 = l & value;
            if (l == l2 || bytes.compareAndSwapLong(longIndex << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet or(long longIndex, long value) {
        while (true) {
            long l = bytes.readVolatileLong(longIndex << 3);
            long l2 = l | value;
            if (l == l2 || bytes.compareAndSwapLong(longIndex << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet xor(long longIndex, long value) {
        while (true) {
            long l = bytes.readVolatileLong(longIndex << 3);
            long l2 = l ^ value;
            if (bytes.compareAndSwapLong(longIndex << 3, l, l2)) return this;
        }
    }

    @Override
    public DirectBitSet andNot(long longIndex, long value) {
        while (true) {
            long l = bytes.readVolatileLong(longIndex << 3);
            long l2 = l & ~value;
            if (bytes.compareAndSwapLong(longIndex << 3, l, l2)) return this;
        }
    }
}
