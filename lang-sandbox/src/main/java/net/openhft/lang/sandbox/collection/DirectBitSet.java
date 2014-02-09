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

import net.openhft.lang.ReferenceCounted;

public interface DirectBitSet extends ReferenceCounted {
    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    DirectBitSet flip(long bitIndex);

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param fromIndex index of the first bit to flip
     * @param toIndex   index after the last bit to flip
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     */
    DirectBitSet flip(long fromIndex, long toIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    DirectBitSet set(long bitIndex);

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index
     * @param value    a boolean value to set
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    DirectBitSet set(long bitIndex, boolean value);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     */
    DirectBitSet set(long fromIndex, long toIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the specified value.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @param value     value to set the selected bits to
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     */
    DirectBitSet set(long fromIndex, long toIndex, boolean value);

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex the index of the bit to be cleared
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    DirectBitSet clear(long bitIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex   index after the last bit to be cleared
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     */
    DirectBitSet clear(long fromIndex, long toIndex);

    /**
     * Sets all of the bits in this BitSet to {@code false}.
     */
    DirectBitSet clear();

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code BitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    boolean get(long bitIndex);

    /**
     * Returns the value of the long with the specified long index.
     *
     * @param longIndex the bit index
     * @return the value of the long with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long getLong(long longIndex);

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     * <p/>
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     * <p/>
     * <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextSetBit(long fromIndex);

    /**
     * Returns the index of the first long that contains a bit set to {@code true}
     * that occurs on or after the specified starting index. The index is the number of longs.
     * If no such bit exists then {@code -1} is returned.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the next set long, or {@code -1} if there
     * is no such long
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextSetLong(long fromLongIndex);

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextClearBit(long fromLongIndex);

    /**
     * Returns the index of the first long that contains a bit is set to {@code false}
     * that occurs on or after the specified starting index. The index in multiples of 64-bit
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next long containing a clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextClearLong(long fromIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     * <p/>
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     * <p/>
     * <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousSetBit(long fromLongIndex);

    /**
     * Returns the index of the nearest long that contains a bit set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousSetLong(long fromLongIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousClearBit(long fromIndex);

    /**
     * Returns the index of the nearest long that contains a bit set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousClearLong(long fromLongIndex);

    /**
     * Returns the "logical size" of this {@code BitSet}: the index of
     * the highest set bit in the {@code BitSet} plus one. Returns zero
     * if the {@code BitSet} contains no set bits.
     *
     * @return the logical size of this {@code BitSet}
     */
    long length();

    /**
     * Returns the number of bits set to {@code true} in this {@code BitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code BitSet}
     */
    long cardinality();

    /**
     * Performs a logical <b>AND</b> of this target bit set with the
     * argument bit set. This bit set is modified so that each bit in it
     * has the value {@code true} if and only if it both initially
     * had the value {@code true} and the corresponding bit in the
     * bit set argument also had the value {@code true}.
     *
     * @param index of long to AND
     * @param value of long to AND
     */
    DirectBitSet and(long index, long value);

    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if it either already had the
     * value {@code true} or the corresponding bit in the bit set
     * argument has the value {@code true}.
     *
     * @param index of long to OR
     * @param value of long to OR
     */
    DirectBitSet or(long index, long value);

    /**
     * Performs a logical <b>XOR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if one of the following
     * statements holds:
     * <ul>
     * <li>The bit initially has the value {@code true}, and the
     * corresponding bit in the argument has the value {@code false}.
     * <li>The bit initially has the value {@code false}, and the
     * corresponding bit in the argument has the value {@code true}.
     * </ul>
     *
     * @param index of long to XOR
     * @param value of long to XOR
     */
    DirectBitSet xor(long index, long value);

    /**
     * Clears all of the bits in this {@code BitSet} whose corresponding
     * bit is set in the specified {@code BitSet}.
     *
     * @param index of long to AND NOT
     * @param value of long to AND NOT
     */
    DirectBitSet andNot(long index, long value);

}
