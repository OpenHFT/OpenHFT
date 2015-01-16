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

package net.openhft.lang.collection;

import net.openhft.lang.io.Bytes;

import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;

/**
 * DirectBitSet with input validations, This class is not thread safe
 */
public class SingleThreadedDirectBitSet implements DirectBitSet {

    public static final long ALL_ONES = ~0L;

    // masks

    static long singleBit(long bitIndex) {
        return 1L << bitIndex;
    }

    static long higherBitsIncludingThis(long bitIndex) {
        return ALL_ONES << bitIndex;
    }

    static long lowerBitsIncludingThis(long bitIndex) {
        return ALL_ONES >>> ~bitIndex;
    }

    static long higherBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES >>> ~bitIndex);
    }

    static long lowerBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES << bitIndex);
    }

    // conversions

    static long longWithThisBit(long bitIndex) {
        return bitIndex >> 6;
    }

    static long firstByte(long longIndex) {
        return longIndex << 3;
    }

    static long firstBit(long longIndex) {
        return longIndex << 6;
    }

    static long lastBit(long longIndex) {
        return firstBit(longIndex) + 63;
    }

    private Bytes bytes;
    private long longLength;

    public SingleThreadedDirectBitSet() {}

    public SingleThreadedDirectBitSet(Bytes bytes) {
        reuse(bytes);
    }

    public void reuse(Bytes bytes) {
        this.bytes = bytes;
        longLength = bytes.capacity() >> 3;
    }

    // checks

    static void checkNumberOfBits(int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException("Illegal number of bits: " + numberOfBits);
    }

    static boolean checkNotFoundIndex(long fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return true;
            throw new IndexOutOfBoundsException();
        }
        return false;
    }

    private void checkIndex(long bitIndex, long longIndex) {
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
    }

    private void checkFromTo(long fromIndex, long exclusiveToIndex, long toLongIndex) {
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();
    }

    private long readLong(long longIndex) {
        return bytes.readLong(firstByte(longIndex));
    }

    private void writeLong(long longIndex, long toWrite) {
        bytes.writeLong(firstByte(longIndex), toWrite);
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
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long byteIndex = firstByte(longIndex);
        long mask = singleBit(bitIndex);
        long l = bytes.readLong(byteIndex);
        long l2 = l ^ mask;
        bytes.writeLong(byteIndex, l2);
        return this;
    }

    @Override
    public DirectBitSet flip(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = bytes.readLong(fromByteIndex);
                long l2 = l ^ mask;
                bytes.writeLong(fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(i, ~readLong(i));
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(i, ~readLong(i));
                }

                long toByteIndex = firstByte(toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = bytes.readLong(toByteIndex);
                long l2 = l ^ mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = bytes.readLong(byteIndex);
            long l2 = l ^ mask;
            bytes.writeLong(byteIndex, l2);
        }
        return this;
    }

    @Override
    public DirectBitSet set(long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long byteIndex = firstByte(longIndex);
        long mask = singleBit(bitIndex);
        long l = bytes.readLong(byteIndex);
        if ((l & mask) != 0) return this;
        long l2 = l | mask;
        bytes.writeLong(byteIndex, l2);
        return this;
    }

    @Override
    public boolean setIfClear(long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long byteIndex = firstByte(longIndex);
        long mask = singleBit(bitIndex);
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
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = bytes.readLong(fromByteIndex);
                long l2 = l | mask;
                bytes.writeLong(fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(i, ALL_ONES);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(i, ALL_ONES);
                }

                long toByteIndex = firstByte(toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = bytes.readLong(toByteIndex);
                long l2 = l | mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = bytes.readLong(byteIndex);
            long l2 = l | mask;
            bytes.writeLong(byteIndex, l2);
        }
        return this;
    }

    @Override
    public DirectBitSet setAll() {
        for (long i = 0; i < longLength; i++) {
            writeLong(i, ALL_ONES);
        }
        return this;
    }

    @Override
    public DirectBitSet set(long fromIndex, long toIndex, boolean value) {
        return value ? set(fromIndex, toIndex) : clear(fromIndex, toIndex);
    }

    @Override
    public DirectBitSet clear(long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long byteIndex = firstByte(longIndex);
        long mask = singleBit(bitIndex);
        long l = bytes.readLong(byteIndex);
        if ((l & mask) == 0) return this;
        long l2 = l & ~mask;
        bytes.writeLong(byteIndex, l2);
        return this;
    }

    @Override
    public boolean clearIfSet(long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long byteIndex = firstByte(longIndex);
        long mask = singleBit(bitIndex);
        long l = bytes.readLong(byteIndex);
        if ((l & mask) == 0) return false;
        long l2 = l & ~mask;
        bytes.writeLong(byteIndex, l2);
        return true;
    }

    @Override
    public DirectBitSet clear(long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = bytes.readLong(fromByteIndex);
                long l2 = l & ~mask;
                bytes.writeLong(fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(i, 0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(i, 0L);
                }

                long toByteIndex = firstByte(toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = bytes.readLong(toByteIndex);
                long l2 = l & ~mask;
                bytes.writeLong(toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
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
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = higherBitsIncludingThis(fromIndex);
                if ((~(readLong(fromLongIndex)) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (~readLong(i) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (~readLong(i) != 0L)
                        return false;
                }

                long mask = lowerBitsIncludingThis(toIndex);
                return ((~readLong(toLongIndex)) & mask) == 0L;
            }
        } else {
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return ((~readLong(fromLongIndex)) & mask) == 0L;
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
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = higherBitsIncludingThis(fromIndex);
                if ((readLong(fromLongIndex) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (readLong(i) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (readLong(i) != 0L)
                        return false;
                }

                long mask = lowerBitsIncludingThis(toIndex);
                return (readLong(toLongIndex) & mask) == 0L;
            }
        } else {
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return (readLong(fromLongIndex) & mask) == 0L;
        }
    }

    @Override
    public DirectBitSet clear() {
        bytes.zeroOut();
        return this;
    }

    @Override
    public boolean get(long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        checkIndex(bitIndex, longIndex);
        long l = readLong(longIndex);
        return (l & (singleBit(bitIndex))) != 0;
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
        checkIndex(longIndex, longIndex);
        return readLong(longIndex);
    }

    @Override
    public long nextSetBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = readLong(fromLongIndex) >>> fromIndex;
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = readLong(i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
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
                int trailingZeros = numberOfTrailingZeros(l);
                currentWord = (l >>> trailingZeros) >>> 1;
                return bitIndex += trailingZeros + 1;
            }
            for (long i = byteIndex, lim = byteLength; (i += 8) < lim;) {
                if ((l = bytes.readLong(i)) != 0) {
                    byteIndex = i;
                    int trailingZeros = numberOfTrailingZeros(l);
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
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = firstByte(fromLongIndex);
        long w = bytes.readLong(fromByteIndex);
        long l = w >>> fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfSetBit);
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(i);
            l = bytes.readLong(byteIndex);
            if (l != 0) {
                long indexOfSetBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfSetBit);
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
        if (readLong(fromLongIndex) != 0)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            if (readLong(i) != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long nextClearBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = (~readLong(fromLongIndex)) >>> fromIndex;
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = ~readLong(i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long setNextClearBit(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = firstByte(fromLongIndex);
        long w = bytes.readLong(fromByteIndex);
        long l = (~w) >>> fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfClearBit);
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(i);
            w = bytes.readLong(byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfClearBit);
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
        if (readLong(fromLongIndex) != ALL_ONES)
            return fromLongIndex;
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            if (readLong(i) != ALL_ONES)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetBit(long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            // the same policy for this "index out of bounds" situation
            // as in j.u.BitSet
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        // << ~fromIndex === << (63 - (fromIndex & 63))
        long l = readLong(fromLongIndex) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = readLong(i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }


    private long previousSetBit(long fromIndex, long inclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toLongIndex = longWithThisBit(inclusiveToIndex);
        checkFromTo(inclusiveToIndex, fromIndex + 1, toLongIndex);
        if (fromLongIndex >= longLength) {
            // the same policy for this "index out of bounds" situation
            // as in j.u.BitSet
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        if (fromLongIndex != toLongIndex) {
            // << ~fromIndex === << (63 - (fromIndex & 63))
            long l = readLong(fromLongIndex) << ~fromIndex;
            if (l != 0)
                return fromIndex - numberOfLeadingZeros(l);
            for (long i = fromLongIndex - 1; i > toLongIndex; i--) {
                l = readLong(i);
                if (l != 0)
                    return lastBit(i) - numberOfLeadingZeros(l);
            }
            fromIndex = lastBit(toLongIndex);
        }
        long w = readLong(toLongIndex);
        long mask = higherBitsIncludingThis(inclusiveToIndex) & lowerBitsIncludingThis(fromIndex);
        long l = w & mask;
        if (l != 0) {
            return lastBit(toLongIndex) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long clearPreviousSetBit(long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        long fromByteIndex = firstByte(fromLongIndex);
        long w = bytes.readLong(fromByteIndex);
        long l = w << ~fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex - numberOfLeadingZeros(l);
            long mask = singleBit(indexOfSetBit);
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(i);
            l = bytes.readLong(byteIndex);
            if (l != 0) {
                long indexOfSetBit = lastBit(i) - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfSetBit);
                bytes.writeLong(byteIndex, l ^ mask);
                return indexOfSetBit;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public long previousSetLong(long fromLongIndex) {
        if (checkNotFoundIndex(fromLongIndex))
            return NOT_FOUND;
        if (fromLongIndex >= longLength)
            fromLongIndex = longLength - 1;
        if (readLong(fromLongIndex) != 0)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            if (readLong(i) != 0)
                return i;
        }
        return NOT_FOUND;
    }

    @Override
    public long previousClearBit(long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        long l = (~readLong(fromLongIndex)) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = ~readLong(i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public long setPreviousClearBit(long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = size() - 1;
        }
        long fromByteIndex = firstByte(fromLongIndex);
        long w = bytes.readLong(fromByteIndex);
        long l = (~w) << ~fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex - numberOfLeadingZeros(l);
            long mask = singleBit(indexOfClearBit);
            bytes.writeLong(fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(i);
            w = bytes.readLong(byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = lastBit(i) - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfClearBit);
                bytes.writeLong(byteIndex, w ^ mask);
                return indexOfClearBit;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public long previousClearLong(long fromLongIndex) {
        if (checkNotFoundIndex(fromLongIndex))
            return NOT_FOUND;
        if (fromLongIndex >= longLength)
            fromLongIndex = longLength - 1;
        if (readLong(fromLongIndex) != ALL_ONES)
            return fromLongIndex;
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            if (readLong(i) != ALL_ONES)
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
            count += Long.bitCount(readLong(i));
        }
        return count;
    }

    @Override
    public DirectBitSet and(long longIndex, long value) {
        long l = readLong(longIndex);
        long l2 = l & value;
        writeLong(longIndex, l2);
        return this;
    }

    @Override
    public DirectBitSet or(long longIndex, long value) {
        long l = readLong(longIndex);
        long l2 = l | value;
        writeLong(longIndex, l2);
        return this;
    }

    @Override
    public DirectBitSet xor(long longIndex, long value) {
        long l = readLong(longIndex);
        long l2 = l ^ value;
        writeLong(longIndex, l2);
        return this;
    }

    @Override
    public DirectBitSet andNot(long longIndex, long value) {
        long l = readLong(longIndex);
        long l2 = l & ~value;
        writeLong(longIndex, l2);
        return this;
    }

    /**
     * @throws IllegalArgumentException if {@code numberOfBits} is negative
     */
    @Override
    public long setNextNContinuousClearBits(long fromIndex, int numberOfBits) {
        if (numberOfBits > 64)
            return setNextManyContinuousClearBits(fromIndex, numberOfBits);
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setNextClearBit(fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        long nTrailingOnes = ALL_ONES >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = longWithThisBit(bitIndex);
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = firstByte(longIndex2);
        long w1, w2 = bytes.readLong(byteIndex2);
        longLoop: while (true) {
            w1 = w2;
            byteIndex2 += 8;
            if (++longIndex2 < longLength) {
                w2 = bytes.readLong(byteIndex2);
            } else if (longIndex2 == longLength) {
                w2 = ALL_ONES;
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
                    int trailingOnes = numberOfTrailingZeros(x);
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
                        long mask2 = (singleBit(bitsFromSecondWordToSwitch)) - 1;
                        bytes.writeLong(byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing zeros > 0
                // > 0 ensured by block (2)
                int trailingZeros = numberOfTrailingZeros(l);
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
                    int trailingOnes = numberOfTrailingZeros(x);
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

    private long setNextManyContinuousClearBits(long fromIndex, int numberOfBits) {
        long size = size();
        long testFromIndex = fromIndex;
        while (true) {
            long limit = fromIndex + numberOfBits;
            if (limit > size)
                return NOT_FOUND;
            long needToBeZerosUntil = limit - 1;
            long lastSetBit = previousSetBit(needToBeZerosUntil, testFromIndex);
            if (lastSetBit == NOT_FOUND) {
                set(fromIndex, limit);
                return fromIndex;
            }
            fromIndex = lastSetBit + 1;
            testFromIndex = limit;
        }
    }

    /**
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long clearNextNContinuousSetBits(long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearNextSetBit(fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        long nTrailingOnes = ALL_ONES >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = longWithThisBit(bitIndex);
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = firstByte(longIndex2);
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
                    int trailingZeros = numberOfTrailingZeros(l);
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
                        long mask2 = (singleBit(bitsFromSecondWordToSwitch)) - 1;
                        bytes.writeLong(byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing ones > 0
                // > 0 ensured by block (2)
                int trailingOnes = numberOfTrailingZeros(~l);
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
                    int trailingZeros = numberOfTrailingZeros(l);
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
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long setPreviousNContinuousClearBits(
            long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setPreviousClearBit(fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = higherBitsIncludingThis(n64Complement);

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = longWithThisBit(fromIndex);
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = firstByte(lowLongIndex);
        // low word, high word
        long hw, lw = bytes.readLong(lowByteIndex);
        longLoop: while (true) {
            hw = lw;
            lowByteIndex -= 8;
            if (--lowLongIndex >= 0) {
                lw = bytes.readLong(lowByteIndex);
            } else if (lowLongIndex == -1) {
                lw = ALL_ONES;
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
                    int leadingOnes = numberOfLeadingZeros(x);
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
                        long lMask = ~(ALL_ONES >>> bitsFromLowWordToSwitch);
                        bytes.writeLong(lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading zeros > 0
                // > 0 ensured by block (2)
                int leadingZeros = numberOfLeadingZeros(l);
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
                    int leadingOnes = numberOfLeadingZeros(x);
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
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public long clearPreviousNContinuousSetBits(
            long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearPreviousSetBit(fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = higherBitsIncludingThis(n64Complement);

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = longWithThisBit(fromIndex);
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = firstByte(lowLongIndex);
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
                int leadingZeros = numberOfLeadingZeros(l);
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
                        long lMask = ~(ALL_ONES >>> bitsFromLowWordToSwitch);
                        bytes.writeLong(lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading ones > 0
                // > 0 ensured by block (2)
                int leadingOnes = numberOfLeadingZeros(~l);
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
                    int leadingZeros = numberOfLeadingZeros(l);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SingleThreadedBitSet{");
        sb.append("size=" + size());
        sb.append(", cardinality=" + cardinality());
        sb.append(", bits=[");
        for (long i = 0L; i < longLength; i++) {
            sb.append(Long.toBinaryString(readLong(i)));
        }
        sb.append("]}");
        return sb.toString();
    }
}
