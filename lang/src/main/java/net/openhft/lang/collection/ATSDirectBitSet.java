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

package net.openhft.lang.collection;

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
            if ((fromIndex & 63) != 0) {
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

            if ((exclusiveToIndex & 63) == 0) {
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
                // >>> ~toIndex === >>> (63 - (toIndex & 63))
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
            if ((fromIndex & 63) != 0) {
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

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~0L);
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 63))
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
    public boolean clearIfSet(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        while (true) {
            long l = bytes.readVolatileLong(byteIndex);
            if ((l & mask) == 0) return false;
            long l2 = l & ~mask;
            if (bytes.compareAndSwapLong(byteIndex, l, l2))
                return true;
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
            if ((fromIndex & 63) != 0) {
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

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    bytes.writeLong(i << 3, 0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    bytes.writeLong(i << 3, 0L);
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 63))
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
    public boolean isSet(long bitIndex) {
        return get(bitIndex);
    }

    @Override
    public boolean isClear(long bitIndex) {
        return !get(bitIndex);
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

    private class SetBits implements Bits {
        private long byteIndex = 0;
        private final long byteLength = longLength << 3;
        private long bitIndex = 0;

        @Override
        public long next() {
            long bitIndex = this.bitIndex;
            if (bitIndex >= 0) {
                long i = byteIndex;
                long l = bytes.readVolatileLong(i) >>> bitIndex;
                if (l != 0) {
                    int trailingZeros = Long.numberOfTrailingZeros(l);
                    long index = bitIndex + trailingZeros;
                    if (((this.bitIndex = index + 1) & 63) == 0) {
                        if ((byteIndex = i + 8) == byteLength)
                            this.bitIndex = -1;
                    }
                    return index;
                }
                for (long lim = byteLength; (i += 8) < lim;) {
                    if ((l = bytes.readLong(i)) != 0) {
                        int trailingZeros = Long.numberOfTrailingZeros(l);
                        long index = (i << 3) + trailingZeros;
                        if (((this.bitIndex = index + 1) & 63) != 0) {
                            byteIndex = i;
                        } else {
                            if ((byteIndex = i + 8) == lim)
                                this.bitIndex = -1;
                        }
                        return index;
                    }
                }
            }
            this.bitIndex = -1;
            return -1;
        }
    }

    @Override
    public Bits setBits() {
        return new SetBits();
    }

    @Override
    public long clearNextSetBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = fromLongIndex << 3;
        while (true) {
            long w = bytes.readVolatileLong(fromByteIndex);
            long l = w >>> fromIndex;
            if (l != 0) {
                long indexOfSetBit = fromIndex + Long.numberOfTrailingZeros(l);
                long mask = 1L << indexOfSetBit;
                if (bytes.compareAndSwapLong(fromByteIndex, w, w ^ mask))
                    return indexOfSetBit;
            } else {
                break;
            }
        }
        longLoop: for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = i << 3;
            while (true) {
                long l = bytes.readLong(byteIndex);
                if (l != 0) {
                    long indexOfSetBit =
                            (i << 6) + Long.numberOfTrailingZeros(l);
                    long mask = 1L << indexOfSetBit;
                    if (bytes.compareAndSwapLong(byteIndex, l, l ^ mask))
                        return indexOfSetBit;
                } else {
                    continue longLoop;
                }
            }
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
    public long setNextClearBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = fromLongIndex << 3;
        while (true) {
            long w = bytes.readVolatileLong(fromByteIndex);
            long l = (~w) >>> fromIndex;
            if (l != 0) {
                long indexOfClearBit =
                        fromIndex + Long.numberOfTrailingZeros(l);
                long mask = 1L << indexOfClearBit;
                if (bytes.compareAndSwapLong(fromByteIndex, w, w ^ mask))
                    return indexOfClearBit;
            } else {
                break;
            }
        }
        longLoop: for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = i << 3;
            while (true) {
                long w = bytes.readLong(byteIndex);
                long l = ~w;
                if (l != 0) {
                    long indexOfClearBit =
                            (i << 6) + Long.numberOfTrailingZeros(l);
                    long mask = 1L << indexOfClearBit;
                    if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask))
                        return indexOfClearBit;
                } else {
                    continue longLoop;
                }
            }
        }
        return NOT_FOUND;
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
        // << ~fromIndex === << (63 - (fromIndex & 63))
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
    public long clearPreviousSetBit(long fromIndex) {
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
        long fromByteIndex = fromLongIndex << 3;
        while (true) {
            long w = bytes.readVolatileLong(fromByteIndex);
            long l = w << ~fromIndex;
            if (l != 0) {
                long indexOfSetBit = fromIndex - Long.numberOfLeadingZeros(l);
                long mask = 1L << indexOfSetBit;
                if (bytes.compareAndSwapLong(fromByteIndex, w, w ^ mask))
                    return indexOfSetBit;
            } else {
                break;
            }
        }
        longLoop: for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = i << 3;
            while (true) {
                long l = bytes.readLong(byteIndex);
                if (l != 0) {
                    long indexOfSetBit =
                            (i << 6) + 63 - Long.numberOfLeadingZeros(l);
                    long mask = 1L << indexOfSetBit;
                    if (bytes.compareAndSwapLong(byteIndex, l, l ^ mask))
                        return indexOfSetBit;
                } else {
                    continue longLoop;
                }
            }
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
    public long setPreviousClearBit(long fromIndex) {
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
        long fromByteIndex = fromLongIndex << 3;
        while (true) {
            long w = bytes.readVolatileLong(fromByteIndex);
            long l = (~w) << ~fromIndex;
            if (l != 0) {
                long indexOfClearBit = fromIndex - Long.numberOfLeadingZeros(l);
                long mask = 1L << indexOfClearBit;
                if (bytes.compareAndSwapLong(fromByteIndex, w, w ^ mask))
                    return indexOfClearBit;
            } else {
                break;
            }
        }
        longLoop: for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = i << 3;
            while (true) {
                long w = bytes.readLong(byteIndex);
                long l = ~w;
                if (l != 0) {
                    long indexOfClearBit =
                            (i << 6) + 63 - Long.numberOfLeadingZeros(l);
                    long mask = 1L << indexOfClearBit;
                    if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask))
                        return indexOfClearBit;
                } else {
                    continue longLoop;
                }
            }
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


    private static long rightShiftOneFill(long l, long shift) {
        return (l >> shift) | ~((~0L) >>> shift);
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws java.lang.IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long setNextNContinuousClearBits(long fromIndex, int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException();
        if (numberOfBits == 1)
            return setNextClearBit(fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        int n64Complement = 64 - numberOfBits;
        long nTrailingOnes = (~0L) >>> n64Complement;

        long bitIndex = fromIndex;
        long longIndex = bitIndex >> 6;
        long byteIndex = longIndex << 3;
        long w, l;
        if ((bitIndex & 63) > n64Complement) {
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = longIndex << 6;
            l = w = bytes.readVolatileLong(byteIndex);
        } else {
            if (longIndex >= longLength)
                return NOT_FOUND;
            w = bytes.readVolatileLong(byteIndex);
            l = rightShiftOneFill(w, bitIndex);
        }
        // long loop
        while (true) {
            continueLongLoop: {
                // (1)
                if ((l & 1) != 0) {
                    long x = ~l;
                    if (x != 0) {
                        int trailingOnes = Long.numberOfTrailingZeros(x);
                        bitIndex += trailingOnes;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        // (2)
                        l = rightShiftOneFill(l, trailingOnes);
                    } else {
                        // all bits are ones, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while ((l & nTrailingOnes) == 0) {
                        long mask = nTrailingOnes << bitIndex;
                        if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask)) {
                            return bitIndex;
                        } else {
                            w = bytes.readLong(byteIndex);
                            l = rightShiftOneFill(w, bitIndex);
                        }
                    }
                    // n > trailing zeros > 0
                    // > 0 ensured by block (1)
                    int trailingZeros = Long.numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    // (3)
                    l = rightShiftOneFill(l, trailingZeros);

                    long x = ~l;
                    if (x != 0) {
                        int trailingOnes = Long.numberOfTrailingZeros(x);
                        bitIndex += trailingOnes;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        // already shifted with one-filling at least once
                        // at (2) or (3), => garanteed highest bit is 1 =>
                        // "natural" one-filling
                        l >>= trailingOnes;
                    } else {
                        // zeros in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = longIndex << 6;
            l = w = bytes.readLong(byteIndex);
        }
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws java.lang.IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long clearNextNContinuousSetBits(long fromIndex, int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException();
        if (numberOfBits == 1)
            return clearNextSetBit(fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        int n64Complement = 64 - numberOfBits;
        long nTrailingOnes = (~0L) >>> n64Complement;

        long bitIndex = fromIndex;
        long longIndex = bitIndex >> 6;
        long byteIndex = longIndex << 3;
        long w, l;
        if ((bitIndex & 63) > n64Complement) {
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = longIndex << 6;
            l = w = bytes.readVolatileLong(byteIndex);
        } else {
            if (longIndex >= longLength)
                return NOT_FOUND;
            w = bytes.readVolatileLong(byteIndex);
            l = w >>> bitIndex;
        }
        // long loop
        while (true) {
            continueLongLoop: {
                if ((l & 1) == 0) {
                    if (l != 0) {
                        int trailingZeros = Long.numberOfTrailingZeros(l);
                        bitIndex += trailingZeros;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        l >>>= trailingZeros;
                    } else {
                        // all bits are zeros, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while (((~l) & nTrailingOnes) == 0) {
                        long mask = nTrailingOnes << bitIndex;
                        if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask)) {
                            return bitIndex;
                        } else {
                            w = bytes.readLong(byteIndex);
                            l = w >>> bitIndex;
                        }
                    }
                    // n > trailing ones > 0
                    int trailingOnes = Long.numberOfTrailingZeros(~l);
                    bitIndex += trailingOnes;
                    l >>>= trailingOnes;

                    if (l != 0) {
                        int trailingZeros = Long.numberOfTrailingZeros(l);
                        bitIndex += trailingZeros;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        l >>>= trailingZeros;
                    } else {
                        // ones in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = longIndex << 6;
            l = w = bytes.readLong(byteIndex);
        }
    }

    private static long leftShiftOneFill(long l, long shift) {
        return (l << shift) | ((1L << shift) - 1L);
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws java.lang.IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long setPreviousNContinuousClearBits(
            long fromIndex, int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException();
        if (numberOfBits == 1)
            return setPreviousClearBit(fromIndex);
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }

        int numberOfBitsMinusOne = numberOfBits - 1;
        long nLeadingOnes = (~0L) << (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex = bitIndex >> 6;
        if (longIndex >= longLength) {
            longIndex = longLength - 1;
            bitIndex = (longIndex << 6) + 63;
        }
        long byteIndex = longIndex << 3;
        long w, l;
        if ((bitIndex & 63) < numberOfBitsMinusOne) {
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = (longIndex << 6) + 63;
            l = w = bytes.readVolatileLong(byteIndex);
        } else {
            w = bytes.readVolatileLong(byteIndex);
            // left shift by ~bitIndex === left shift by (63 - (bitIndex & 63))
            l = leftShiftOneFill(w, ~bitIndex);
        }
        // long loop
        while (true) {
            continueLongLoop: {
                if (l < 0) { // condition means the highest bit is one
                    long x = ~l;
                    if (x != 0) {
                        int leadingOnes = Long.numberOfLeadingZeros(x);
                        bitIndex -= leadingOnes;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l = leftShiftOneFill(l, leadingOnes);
                    } else {
                        // all bits are ones, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while ((l & nLeadingOnes) == 0) {
                        // >>> ~bitIndex === >>> (63 - (butIndex & 63))
                        long mask = nLeadingOnes >>> ~bitIndex;
                        if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask)) {
                            return bitIndex - numberOfBitsMinusOne;
                        } else {
                            w = bytes.readLong(byteIndex);
                            l = leftShiftOneFill(w, ~bitIndex);
                        }
                    }
                    // n > leading zeros > 0
                    int leadingZeros = Long.numberOfLeadingZeros(l);
                    bitIndex -= leadingZeros;
                    l = leftShiftOneFill(l, leadingZeros);

                    long x = ~l;
                    if (x != 0) {
                        int leadingOnes = Long.numberOfLeadingZeros(x);
                        bitIndex -= leadingOnes;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l = leftShiftOneFill(l, leadingOnes);
                    } else {
                        // zeros in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = (longIndex << 6) + 63;
            l = w = bytes.readLong(byteIndex);
        }
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws java.lang.IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long clearPreviousNContinuousSetBits(
            long fromIndex, int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException();
        if (numberOfBits == 1)
            return clearPreviousSetBit(fromIndex);
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return NOT_FOUND;
            throw new IndexOutOfBoundsException();
        }

        int numberOfBitsMinusOne = numberOfBits - 1;
        long nLeadingOnes = (~0L) << (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex = bitIndex >> 6;
        if (longIndex >= longLength) {
            longIndex = longLength - 1;
            bitIndex = (longIndex << 6) + 63;
        }
        long byteIndex = longIndex << 3;
        long w, l;
        if ((bitIndex & 63) < numberOfBitsMinusOne) {
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = (longIndex << 6) + 63;
            l = w = bytes.readVolatileLong(byteIndex);
        } else {
            w = bytes.readVolatileLong(byteIndex);
            // << ~bitIndex === << (63 - (bitIndex & 63))
            l = w << ~bitIndex;
        }
        // long loop
        while (true) {
            continueLongLoop: {
                // condition means the highest bit is zero, but not all
                if (l > 0) {
                    int leadingZeros = Long.numberOfLeadingZeros(l);
                    bitIndex -= leadingZeros;
                    if ((bitIndex & 63) < numberOfBitsMinusOne)
                        break continueLongLoop;
                    l <<= leadingZeros;
                } else if (l == 0) {
                    // all bits are zeros, go to the next long
                    break continueLongLoop;
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while (((~l) & nLeadingOnes) == 0) {
                        // >>> ~bitIndex === >>> (63 - (butIndex & 63))
                        long mask = nLeadingOnes >>> ~bitIndex;
                        if (bytes.compareAndSwapLong(byteIndex, w, w ^ mask)) {
                            return bitIndex - numberOfBitsMinusOne;
                        } else {
                            w = bytes.readLong(byteIndex);
                            l = w << ~bitIndex;
                        }
                    }
                    // n > leading ones > 0
                    int leadingOnes = Long.numberOfLeadingZeros(~l);
                    bitIndex -= leadingOnes;
                    l <<= leadingOnes;

                    if (l != 0) {
                        int leadingZeros = Long.numberOfLeadingZeros(l);
                        bitIndex -= leadingZeros;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l <<= leadingZeros;
                    } else {
                        // ones in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = (longIndex << 6) + 63;
            l = w = bytes.readLong(byteIndex);
        }
    }
}
