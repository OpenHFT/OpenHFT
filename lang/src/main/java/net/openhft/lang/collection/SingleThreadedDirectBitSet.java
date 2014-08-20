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
 * DirectBitSet with input validations, This class is not thread safe
 */
public class SingleThreadedDirectBitSet implements DirectBitSet {
    private final Bytes bytes;
    private final long longLength;

    public SingleThreadedDirectBitSet(Bytes bytes) {
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
        long l = bytes.readLong(byteIndex);
        long l2 = l ^ mask;
        bytes.writeLong(byteIndex, l2);
        return this;
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
                long l = bytes.readLong(fromByteIndex);
                long l2 = l ^ mask;
                bytes.writeLong(fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~bytes.readLong(i << 3));
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    bytes.writeLong(i << 3, ~bytes.readLong(i << 3));
                }

                long toByteIndex = toLongIndex << 3;
                // >>> ~toIndex === >>> (63 - (toIndex & 63))
                long mask = (~0L) >>> ~toIndex;
                long l = bytes.readLong(toByteIndex);
                long l2 = l ^ mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            long l = bytes.readLong(byteIndex);
            long l2 = l ^ mask;
            bytes.writeLong(byteIndex, l2);
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
        long l = bytes.readLong(byteIndex);
        if ((l & mask) != 0) return this;
        long l2 = l | mask;
        bytes.writeLong(byteIndex, l2);
        return this;
    }

    @Override
    public boolean setIfClear(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        long l = bytes.readLong(byteIndex);
        long l2 = l | mask;
        if (l == l2)
            return false;
        bytes.writeLong(byteIndex, l2);
        return true;
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
                long l = bytes.readLong(fromByteIndex);
                long l2 = l | mask;
                bytes.writeLong(fromByteIndex, l2);
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
                long l = bytes.readLong(toByteIndex);
                long l2 = l | mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            long l = bytes.readLong(byteIndex);
            long l2 = l | mask;
            bytes.writeLong(byteIndex, l2);
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
        long l = bytes.readLong(byteIndex);
        if ((l & mask) == 0) return this;
        long l2 = l & ~mask;
        bytes.writeLong(byteIndex, l2);
        return this;
    }

    @Override
    public boolean clearIfSet(long bitIndex) {
        long longIndex = bitIndex >> 6;
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = longIndex << 3;
        long mask = 1L << bitIndex;
        long l = bytes.readLong(byteIndex);
        if ((l & mask) == 0) return false;
        long l2 = l & ~mask;
        bytes.writeLong(byteIndex, l2);
        return true;
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
                long mask = (~0L) << fromIndex;
                long l = bytes.readLong(fromByteIndex);
                long l2 = l & ~mask;
                bytes.writeLong(fromByteIndex, l2);
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
                long mask = (~0L) >>> ~toIndex;
                long l = bytes.readLong(toByteIndex);
                long l2 = l & ~mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = fromLongIndex << 3;
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            long l = bytes.readLong(byteIndex);
            long l2 = l & ~mask;
            bytes.writeLong(byteIndex, l2);
        }
        return this;
    }

    /**
     * Checks if each bit from the specified {@code fromIndex} (inclusive)
     * to the specified {@code exclusiveToIndex} is set to {@code true}.
     *
     * @param fromIndex index of the first bit to check
     * @param exclusiveToIndex index after the last bit to check
     * @return {@code true} if all bits in the specified range are
     *         set to {@code true}, {@code false} otherwise
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code fromIndex} is larger than {@code toIndex},
     *         or {@code exclusiveToIndex} is larger or equal to {@code size()}
     */
    public boolean allSet(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = fromIndex >> 6;
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = toIndex >> 6;
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = (~0L) << fromIndex;
                if ((~(bytes.readLong(fromLongIndex << 3)) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (~bytes.readLong(i << 3) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (~bytes.readLong(i << 3) != 0L)
                        return false;
                }

                // >>> ~toIndex === >>> (63 - (toIndex & 63))
                long mask = (~0L) >>> ~toIndex;
                return ((~bytes.readLong(toLongIndex << 3)) & mask) == 0L;
            }
        } else {
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            return ((~bytes.readLong(fromLongIndex << 3)) & mask) == 0L;
        }
    }

    /**
     * Checks if each bit from the specified {@code fromIndex} (inclusive)
     * to the specified {@code exclusiveToIndex} is set to {@code false}.
     *
     * @param fromIndex index of the first bit to check
     * @param exclusiveToIndex index after the last bit to check
     * @return {@code true} if all bits in the specified range are
     *         set to {@code false}, {@code false} otherwise
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code fromIndex} is larger than {@code toIndex},
     *         or {@code exclusiveToIndex} is larger or equal to {@code size()}
     */
    public boolean allClear(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = fromIndex >> 6;
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = toIndex >> 6;
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = (~0L) << fromIndex;
                if ((bytes.readLong(fromLongIndex << 3) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (bytes.readLong(i << 3) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (bytes.readLong(i << 3) != 0L)
                        return false;
                }

                // >>> ~toIndex === >>> (63 - (toIndex & 63))
                long mask = (~0L) >>> ~toIndex;
                return (bytes.readLong(toLongIndex << 3) & mask) == 0L;
            }
        } else {
            long mask = ((~0L) << fromIndex) & ((~0L) >>> ~toIndex);
            return (bytes.readLong(fromLongIndex << 3) & mask) == 0L;
        }
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
        long l = bytes.readLong(longIndex << 3);
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
        return bytes.readLong(longIndex << 3);
    }

    @Override
    public long nextSetBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = fromIndex >> 6;
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = bytes.readLong(fromLongIndex << 3) >>> fromIndex;
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
        private long bitIndex = -1;
        private long currentWord = bytes.readLong(0);

        @Override
        public long next() {
            long l;
            if ((l = currentWord) != 0) {
                int trailingZeros = Long.numberOfTrailingZeros(l);
                currentWord = (l >>> trailingZeros) >>> 1;
                return bitIndex += trailingZeros + 1;
            }
            for (long i = byteIndex, lim = byteLength; (i += 8) < lim;) {
                if ((l = bytes.readLong(i)) != 0) {
                    byteIndex = i;
                    int trailingZeros = Long.numberOfTrailingZeros(l);
                    currentWord = (l >>> trailingZeros) >>> 1;
                    return bitIndex = (i << 3) + trailingZeros;
                }
            }
            currentWord = 0;
            byteIndex = byteLength;
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
        long w = bytes.readLong(fromByteIndex);
        long l = w >>> fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex + Long.numberOfTrailingZeros(l);
            long mask = 1L << indexOfSetBit;
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = i << 3;
            l = bytes.readLong(byteIndex);
            if (l != 0) {
                long indexOfSetBit = (i << 6) + Long.numberOfTrailingZeros(l);
                long mask = 1L << indexOfSetBit;
                bytes.writeLong(byteIndex, l ^ mask);
                return indexOfSetBit;
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
        if (bytes.readLong(fromLongIndex << 3) != 0)
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
        long l = (~bytes.readLong(fromLongIndex << 3)) >>> fromIndex;
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
        long w = bytes.readLong(fromByteIndex);
        long l = (~w) >>> fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex + Long.numberOfTrailingZeros(l);
            long mask = 1L << indexOfClearBit;
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = i << 3;
            w = bytes.readLong(byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = (i << 6) + Long.numberOfTrailingZeros(l);
                long mask = 1L << indexOfClearBit;
                bytes.writeLong(byteIndex, w ^ mask);
                return indexOfClearBit;
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
        if (bytes.readLong(fromLongIndex << 3) != ~0L)
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
        long l = bytes.readLong(fromLongIndex << 3) << ~fromIndex;
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
        long w = bytes.readLong(fromByteIndex);
        long l = w << ~fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex - Long.numberOfLeadingZeros(l);
            long mask = 1L << indexOfSetBit;
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = i << 3;
            l = bytes.readLong(byteIndex);
            if (l != 0) {
                long indexOfSetBit =
                        (i << 6) + 63 - Long.numberOfLeadingZeros(l);
                long mask = 1L << indexOfSetBit;
                bytes.writeLong(byteIndex, l ^ mask);
                return indexOfSetBit;
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
        if (bytes.readLong(fromLongIndex << 3) != 0)
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
        long l = (~bytes.readLong(fromLongIndex << 3)) << ~fromIndex;
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
        long w = bytes.readLong(fromByteIndex);
        long l = (~w) << ~fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex - Long.numberOfLeadingZeros(l);
            long mask = 1L << indexOfClearBit;
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = i << 3;
            w = bytes.readLong(byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit =
                        (i << 6) + 63 - Long.numberOfLeadingZeros(l);
                long mask = 1L << indexOfClearBit;
                bytes.writeLong(byteIndex, w ^ mask);
                return indexOfClearBit;
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
        if (bytes.readLong(fromLongIndex << 3) != ~0L)
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
        long count = 0;
        for (long i = 0; i < longLength; i++) {
            count += Long.bitCount(bytes.readLong(i << 3));
        }
        return count;
    }

    @Override
    public DirectBitSet and(long longIndex, long value) {
        long l = bytes.readLong(longIndex << 3);
        long l2 = l & value;
        bytes.writeLong(longIndex << 3, l2);
        return this;
    }

    @Override
    public DirectBitSet or(long longIndex, long value) {
        long l = bytes.readLong(longIndex << 3);
        long l2 = l | value;
        bytes.writeLong(longIndex << 3, l2);
        return this;
    }

    @Override
    public DirectBitSet xor(long longIndex, long value) {
        long l = bytes.readLong(longIndex << 3);
        long l2 = l ^ value;
        bytes.writeLong(longIndex << 3, l2);
        return this;
    }

    @Override
    public DirectBitSet andNot(long longIndex, long value) {
        long l = bytes.readLong(longIndex << 3);
        long l2 = l & ~value;
        bytes.writeLong(longIndex << 3, l2);
        return this;
    }

    /**
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

        long nTrailingOnes = (~0L) >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = bitIndex >> 6;
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = longIndex2 << 3;
        long w1, w2 = bytes.readLong(byteIndex2);
        longLoop: while (true) {
            w1 = w2;
            byteIndex2 += 8;
            if (++longIndex2 < longLength) {
                w2 = bytes.readLong(byteIndex2);
            } else if (longIndex2 == longLength) {
                w2 = ~0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            // (1)
            if (bitsFromFirstWord != 64) {
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
            } else {
                // special case, because if bitsFromFirstWord is 64
                // w2 shift is overflowed
                l = w1;
            }
            // (2)
            if ((l & 1) != 0) {
                long x = ~l;
                if (x != 0) {
                    int trailingOnes = Long.numberOfTrailingZeros(x);
                    bitIndex += trailingOnes;
                    // (3)
                    if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                        bitsFromFirstWord += 64;
                        continue; // long loop
                    }
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue; // long loop
                }
            }
            while (true) {
                if ((l & nTrailingOnes) == 0) {
                    long mask1 = nTrailingOnes << bitIndex;
                    bytes.writeLong(byteIndex2 - 8, w1 ^ mask1);
                    int bitsFromSecondWordToSwitch =
                            numberOfBits - bitsFromFirstWord;
                    if (bitsFromSecondWordToSwitch > 0) {
                        long mask2 = (1L << bitsFromSecondWordToSwitch) - 1;
                        bytes.writeLong(byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing zeros > 0
                // > 0 ensured by block (2)
                int trailingZeros = Long.numberOfTrailingZeros(l);
                bitIndex += trailingZeros;
                // (4)
                if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                    bitsFromFirstWord += 64;
                    continue longLoop;
                }
                // (5)
                // subtractions (3) and (4) together ensure that
                // bitsFromFirstWord != 64, => no need in condition like (1)
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);

                long x = ~l;
                if (x != 0) {
                    int trailingOnes = Long.numberOfTrailingZeros(x);
                    bitIndex += trailingOnes;
                    if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                        bitsFromFirstWord += 64;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
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

        long nTrailingOnes = (~0L) >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = bitIndex >> 6;
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = longIndex2 << 3;
        long w1, w2 = bytes.readLong(byteIndex2);
        longLoop: while (true) {
            w1 = w2;
            byteIndex2 += 8;
            if (++longIndex2 < longLength) {
                w2 = bytes.readLong(byteIndex2);
            } else if (longIndex2 == longLength) {
                w2 = 0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            // (1)
            if (bitsFromFirstWord != 64) {
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
            } else {
                // special case, because if bitsFromFirstWord is 64
                // w2 shift is overflowed
                l = w1;
            }
            // (2)
            if ((l & 1) == 0) {
                if (l != 0) {
                    int trailingZeros = Long.numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    // (3)
                    if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                        bitsFromFirstWord += 64;
                        continue; // long loop
                    }
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue; // long loop
                }
            }
            while (true) {
                if (((~l) & nTrailingOnes) == 0) {
                    long mask1 = nTrailingOnes << bitIndex;
                    bytes.writeLong(byteIndex2 - 8, w1 ^ mask1);
                    int bitsFromSecondWordToSwitch =
                            numberOfBits - bitsFromFirstWord;
                    if (bitsFromSecondWordToSwitch > 0) {
                        long mask2 = (1L << bitsFromSecondWordToSwitch) - 1;
                        bytes.writeLong(byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing ones > 0
                // > 0 ensured by block (2)
                int trailingOnes = Long.numberOfTrailingZeros(~l);
                bitIndex += trailingOnes;
                // (4)
                if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                    bitsFromFirstWord += 64;
                    continue longLoop;
                }
                // (5)
                // subtractions (3) and (4) together ensure that
                // bitsFromFirstWord != 64, => no need in condition like (1)
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);

                if (l != 0) {
                    int trailingZeros = Long.numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                        bitsFromFirstWord += 64;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
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

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = (~0L) << n64Complement;

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = fromIndex >> 6;
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = lowLongIndex << 3;
        // low word, high word
        long hw, lw = bytes.readLong(lowByteIndex);
        longLoop: while (true) {
            hw = lw;
            lowByteIndex -= 8;
            if (--lowLongIndex >= 0) {
                lw = bytes.readLong(lowByteIndex);
            } else if (lowLongIndex == -1) {
                lw = ~0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            if (bitsFromLowWord != 0) { // (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else {
                // all bits from high word, special case needed because
                // higherBitBound is multiple of 64 and lw not shifted away
                l = hw;
            }
            // (2)
            if (l < 0) { // condition means the highest bit is one
                long x = ~l;
                if (x != 0) {
                    int leadingOnes = Long.numberOfLeadingZeros(x);
                    higherBitBound -= leadingOnes;
                    bitsFromLowWord += leadingOnes; // (3)
                    int flw;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue; // long loop
                    }
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue; // long loop
                }
            }
            while (true) {
                if ((l & nLeadingOnes) == 0) {
                    long hMask = nLeadingOnes >>> bitsFromLowWord;
                    bytes.writeLong(lowByteIndex + 8, hw ^ hMask);
                    // bitsFromLow - (64 - n) = n - (64 - bitsFromLow) =
                    // = n - bitsFromHigh
                    int bitsFromLowWordToSwitch =
                            bitsFromLowWord - n64Complement;
                    if (bitsFromLowWordToSwitch > 0) {
                        long lMask = ~((~0L) >>> bitsFromLowWordToSwitch);
                        bytes.writeLong(lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading zeros > 0
                // > 0 ensured by block (2)
                int leadingZeros = Long.numberOfLeadingZeros(l);
                higherBitBound -= leadingZeros;
                bitsFromLowWord += leadingZeros; // (4)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue longLoop;
                }
                // (5)
                // additions (3) and (4) together ensure that
                // bitsFromFirstWord > 0, => no need in condition like (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);

                long x = ~l;
                if (x != 0) {
                    int leadingOnes = Long.numberOfLeadingZeros(x);
                    higherBitBound -= leadingOnes;
                    bitsFromLowWord += leadingOnes;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
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

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = (~0L) << n64Complement;

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = fromIndex >> 6;
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = lowLongIndex << 3;
        // low word, high word
        long hw, lw = bytes.readLong(lowByteIndex);
        longLoop: while (true) {
            hw = lw;
            lowByteIndex -= 8;
            if (--lowLongIndex >= 0) {
                lw = bytes.readLong(lowByteIndex);
            } else if (lowLongIndex == -1) {
                lw = 0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            if (bitsFromLowWord != 0) { // (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else {
                // all bits from high word, special case needed because
                // higherBitBound is multiple of 64 and lw not shifted away
                l = hw;
            }
            // (2)
            if (l > 0) { // condition means the highest bit is zero, but not all
                int leadingZeros = Long.numberOfLeadingZeros(l);
                higherBitBound -= leadingZeros;
                bitsFromLowWord += leadingZeros; // (3)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue; // long loop
                }
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else if (l == 0) {
                // all bits are zeros, skip a whole word,
                // bitsFromLowWord not changed
                higherBitBound -= 64;
                continue; // long loop
            }
            while (true) {
                if (((~l) & nLeadingOnes) == 0) {
                    long hMask = nLeadingOnes >>> bitsFromLowWord;
                    bytes.writeLong(lowByteIndex + 8, hw ^ hMask);
                    // bitsFromLow - (64 - n) = n - (64 - bitsFromLow) =
                    // = n - bitsFromHigh
                    int bitsFromLowWordToSwitch =
                            bitsFromLowWord - n64Complement;
                    if (bitsFromLowWordToSwitch > 0) {
                        long lMask = ~((~0L) >>> bitsFromLowWordToSwitch);
                        bytes.writeLong(lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading ones > 0
                // > 0 ensured by block (2)
                int leadingOnes = Long.numberOfLeadingZeros(~l);
                higherBitBound -= leadingOnes;
                bitsFromLowWord += leadingOnes; // (4)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue longLoop;
                }
                // (5)
                // additions (3) and (4) together ensure that
                // bitsFromFirstWord > 0, => no need in condition like (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);


                if (l != 0) {
                    int leadingZeros = Long.numberOfLeadingZeros(l);
                    higherBitBound -= leadingZeros;
                    bitsFromLowWord += leadingZeros;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue longLoop;
                }
            }
        }
    }
}
