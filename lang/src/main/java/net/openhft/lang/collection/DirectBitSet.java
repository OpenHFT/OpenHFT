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

import net.openhft.lang.ReferenceCounted;

/**
 * Most notable logical difference of this interface with {@code java.util.BitSet}
 * is that {@code DirectBitSet} has a rigid {@link #size},
 * attempts to {@link #get}, {@link #set} or {@link #clear} bits at indices
 * exceeding the size cause {@code IndexOutOfBoundsException}. There is also
 * a {@link #setAll()} method to set all bits within the size.
 * {@code java.util.BitSet} doesn't have such rigid capacity.
 *
 * @see java.util.BitSet
 */
public interface DirectBitSet extends ReferenceCounted {
    /**
     * Returned if no entry is found
     */
    public static final long NOT_FOUND = -1L;

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
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
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    DirectBitSet flip(long fromIndex, long toIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
     */
    DirectBitSet set(long bitIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @return true if the bit was zeroOut, or false if the bit was already set.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
     */
    boolean setIfClear(long bitIndex);

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index
     * @param value    a boolean value to set
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
     */
    DirectBitSet set(long bitIndex, boolean value);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    DirectBitSet set(long fromIndex, long toIndex);

    /**
     * Sets all bits, {@code bs.setAll()} is equivalent
     * of {@code bs.set(0, bs.size()}.
     *
     * @return this bit set back
     */
    DirectBitSet setAll();

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the specified value.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @param value     value to set the selected bits to
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    DirectBitSet set(long fromIndex, long toIndex, boolean value);

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex the index of the bit to be cleared
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
     */
    DirectBitSet clear(long bitIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex   index after the last bit to be cleared
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    DirectBitSet clear(long fromIndex, long toIndex);

    /**
     * Sets all of the bits in this BitSet to {@code false}.
     */
    DirectBitSet clear();

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code DirectBitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size())}
     */
    boolean get(long bitIndex);

    /**
     * Returns the value of the long with the specified long index.
     *
     * @param longIndex the bit index
     * @return the value of the long with the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index &lt; 0 || index &gt;= size() / 64)}
     */
    long getLong(long longIndex);

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     * &lt;p/&gt;
     * &lt;p&gt;To iterate over the {@code true} bits in a {@code DirectBitSet},
     * use the following loop:
     * &lt;p/&gt;
     * &lt;pre&gt; {@code
     * for (int i = bs.nextSetBit(0); i &gt;= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     * }}&lt;/pre&gt;
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
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next zeroOut bit,
     * or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextClearBit(long fromIndex);

    /**
     * Returns the index of the first long that contains a bit is set to {@code false}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the next long containing a zeroOut bit,
     * or {@code -1} if there is no such long
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    long nextClearLong(long fromLongIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     * &lt;p/&gt;
     * &lt;p&gt;To iterate over the {@code true} bits in a {@code DirectBitSet},
     * use the following loop:
     * &lt;p/&gt;
     * &lt;pre&gt; {@code
     * for (int i = bs.size(); (i = bs.previousSetBit(i-1)) &gt;= 0; ) {
     *     // operate on index i here
     * }}&lt;/pre&gt;
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousSetBit(long fromIndex);

    /**
     * Returns the index of the nearest long that contains a bit set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromLongIndex the index to start checking from (inclusive)
     * @return the index of the previous long containing a set bit,
     * or {@code -1} if there is no such long
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
     * @return the index of the previous zeroOut bit, or {@code -1} if there
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
     * @return the index of the previous long containing a zeroOut bit,
     * or {@code -1} if there is no such long
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     */
    long previousClearLong(long fromLongIndex);

    /**
     * Returns the number of bits of space actually in use by this BitSet to represent bit values.
     * The index of the last bit in the set eligible to be set or zeroOut
     * is {@code size() - 1}.
     *
     * @return the number of bits in this bit set
     */
    long size();

    /**
     * Returns the number of bits set to {@code true} in this {@code DirectBitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code DirectBitSet}
     */
    long cardinality();

    /**
     * Performs a logical &lt;b&gt;AND&lt;/b&gt; of the long at the specified index in this
     * bit set with the argument long value.
     *
     * @param longIndex of long to AND
     * @param value     of long to AND
     */
    DirectBitSet and(long longIndex, long value);

    /**
     * Performs a logical &lt;b&gt;OR&lt;/b&gt; of the long at the specified index in this
     * bit set with the argument long value.
     *
     * @param longIndex of long to OR
     * @param value     of long to OR
     */
    DirectBitSet or(long longIndex, long value);

    /**
     * Performs a logical &lt;b&gt;XOR&lt;/b&gt; of the long at the specified index in this
     * bit set with the argument long value.
     *
     * @param longIndex of long to XOR
     * @param value     of long to XOR
     */
    DirectBitSet xor(long longIndex, long value);

    /**
     * Clears all of the bits in the long at the specified index in this
     * {@code DirectBitSet} whose corresponding bit is set in the specified
     * long value.
     *
     * @param longIndex of long to AND NOT
     * @param value     of long to AND NOT
     */
    DirectBitSet andNot(long longIndex, long value);

    /**
     * Set a cleared bit and return which bit was set.
     *
     * @param fromIndex first bit to search from
     * @return the bit set.
     */
    long setNFrom(long fromIndex, int numberOfBits);
}
