/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
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

/*
 * Based on java.util.concurrent.TimeUnit, which is
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package net.openhft.lang;

import java.util.concurrent.TimeUnit;

/**
 * A {@code MemoryUnit} represents memory amounts at a given unit of
 * granularity and provides utility methods to convert across units.  A
 * {@code MemoryUnit} does not maintain memory information, but only
 * helps organize and use memory amounts representations that may be maintained
 * separately across various contexts.
 *
 * <p>Note than in this class kilo-, mega- and giga- prefixes means 2^10 = 1024 multiplexing,
 * that is more common in low-level programming, CPU and operation system contexts,
 * not 1000 as defined by International System of Units (SI).
 *
 * <p>A {@code MemoryUnit} is mainly used to inform memory amount-based methods
 * how a given memory amount parameter should be interpreted.
 *
 * <p>API of {@code MemoryUnit} is copied from {@link TimeUnit} enum.
 */
public enum MemoryUnit {

    /**
     * Memory unit representing one bit.
     */
    BITS {
        @Override public long toBits(long d)       { return d; }
        @Override public long toBytes(long d)      { return d/(C1/C0); }
        @Override public long toLongs(long d)      { return d/(C2/C0); }
        @Override public long toCacheLines(long d) { return d/(C3/C0); }
        @Override public long toKilobytes(long d)  { return d/(C4/C0); }
        @Override public long toMegabytes(long d)  { return d/(C5/C0); }
        @Override public long toGigabytes(long d)  { return d/(C6/C0); }
        @Override public long convert(long d, MemoryUnit u) { return u.toBits(d); }
    },

    /**
     * Memory unit representing one byte, i. e. 8 bits.
     */
    BYTES {
        @Override public long toBits(long d)       { return x(d, C1/C0, MAX/(C1/C0)); }
        @Override public long toBytes(long d)      { return d; }
        @Override public long toLongs(long d)      { return d/(C2/C1); }
        @Override public long toCacheLines(long d) { return d/(C3/C1); }
        @Override public long toKilobytes(long d)  { return d/(C4/C1); }
        @Override public long toMegabytes(long d)  { return d/(C5/C1); }
        @Override public long toGigabytes(long d)  { return d/(C6/C1); }
        @Override public long convert(long d, MemoryUnit u) { return u.toBytes(d); }
    },

    /**
     * Memory unit representing 8 bytes, i. e. 64-bit word,
     * the width of Java's primitive {@code long} type.
     */
    LONGS {
        @Override public long toBits(long d)       { return x(d, C2/C0, MAX/(C2/C0)); }
        @Override public long toBytes(long d)      { return x(d, C2/C1, MAX/(C2/C1)); }
        @Override public long toLongs(long d)      { return d; }
        @Override public long toCacheLines(long d) { return d/(C3/C2); }
        @Override public long toKilobytes(long d)  { return d/(C4/C2); }
        @Override public long toMegabytes(long d)  { return d/(C5/C2); }
        @Override public long toGigabytes(long d)  { return d/(C6/C2); }
        @Override public long convert(long d, MemoryUnit u) { return u.toLongs(d); }
    },

    /**
     * Memory unit representing 64 bytes, i. e. most common CPU cache line size.
     */
    CACHE_LINES {
        @Override public long toBits(long d)       { return x(d, C3/C0, MAX/(C3/C0)); }
        @Override public long toBytes(long d)      { return x(d, C3/C1, MAX/(C3/C1)); }
        @Override public long toLongs(long d)      { return x(d, C3/C2, MAX/(C3/C2)); }
        @Override public long toCacheLines(long d) { return d; }
        @Override public long toKilobytes(long d)  { return d/(C4/C3); }
        @Override public long toMegabytes(long d)  { return d/(C5/C3); }
        @Override public long toGigabytes(long d)  { return d/(C6/C3); }
        @Override public long convert(long d, MemoryUnit u) { return u.toCacheLines(d); }
    },

    /**
     * Memory unit representing 1024 bytes.
     */
    KILOBYTES {
        @Override public long toBits(long d)       { return x(d, C4/C0, MAX/(C4/C0)); }
        @Override public long toBytes(long d)      { return x(d, C4/C1, MAX/(C4/C1)); }
        @Override public long toLongs(long d)      { return x(d, C4/C2, MAX/(C4/C2)); }
        @Override public long toCacheLines(long d) { return x(d, C4/C3, MAX/(C4/C3)); }
        @Override public long toKilobytes(long d)  { return d; }
        @Override public long toMegabytes(long d)  { return d/(C5/C4); }
        @Override public long toGigabytes(long d)  { return d/(C6/C4); }
        @Override public long convert(long d, MemoryUnit u) { return u.toKilobytes(d); }
    },

    /**
     * Memory unit representing 1024 kilobytes.
     */
    MEGABYTES {
        @Override public long toBits(long d)       { return x(d, C5/C0, MAX/(C5/C0)); }
        @Override public long toBytes(long d)      { return x(d, C5/C1, MAX/(C5/C1)); }
        @Override public long toLongs(long d)      { return x(d, C5/C2, MAX/(C5/C2)); }
        @Override public long toCacheLines(long d) { return x(d, C5/C3, MAX/(C5/C3)); }
        @Override public long toKilobytes(long d)  { return x(d, C5/C4, MAX/(C5/C4)); }
        @Override public long toMegabytes(long d)  { return d; }
        @Override public long toGigabytes(long d)  { return d/(C6/C5); }
        @Override public long convert(long d, MemoryUnit u) { return u.toMegabytes(d); }
    },

    /**
     * Memory unit representing 1024 megabytes.
     */
    GIGABYTES {
        @Override public long toBits(long d)       { return x(d, C6/C0, MAX/(C6/C0)); }
        @Override public long toBytes(long d)      { return x(d, C6/C1, MAX/(C6/C1)); }
        @Override public long toLongs(long d)      { return x(d, C6/C2, MAX/(C6/C2)); }
        @Override public long toCacheLines(long d) { return x(d, C6/C3, MAX/(C6/C3)); }
        @Override public long toKilobytes(long d)  { return x(d, C6/C4, MAX/(C6/C4)); }
        @Override public long toMegabytes(long d)  { return x(d, C6/C5, MAX/(C6/C5)); }
        @Override public long toGigabytes(long d)  { return d; }
        @Override public long convert(long d, MemoryUnit u) { return u.toGigabytes(d); }
    };

    // Handy constants for conversion methods
    static final long C0 = 1L;
    static final long C1 = C0 * 8L;
    static final long C2 = C1 * 8L;
    static final long C3 = C2 * 8L;
    static final long C4 = C3 * 16L;
    static final long C5 = C4 * 1024L;
    static final long C6 = C5 * 1024L;

    static final long MAX = Long.MAX_VALUE;

    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     */
    static long x(long d, long m, long over) {
        if (d >  over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    // To maintain full signature compatibility with 1.5, and to improve the
    // clarity of the generated javadoc (see 6287639: Abstract methods in
    // enum classes should not be listed as abstract), method convert
    // etc. are not declared abstract but otherwise act as abstract methods.

    /**
     * Converts the given memory amount in the given unit to this unit.
     * Conversions from finer to coarser granularities truncate, so
     * lose precision. For example, converting {@code 7} bits
     * to bytes results in {@code 0}. Conversions from coarser to
     * finer granularities with arguments that would numerically
     * overflow saturate to {@code Long.MIN_VALUE} if negative or
     * {@code Long.MAX_VALUE} if positive.
     *
     * <p>For example, to convert 4096 bytes to cache lines, use:
     * {@code MemoryUnit.CACHE_LINES.convert(4096L, MemoryUnit.BYTES)}
     *
     * @param sourceAmount the memory amount in the given {@code sourceUnit}
     * @param sourceUnit the unit of the {@code sourceAmount} argument
     * @return the converted amount in this unit,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long convert(long sourceAmount, MemoryUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) BITS.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toBits(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) BYTES.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toBytes(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) LONGS.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toLongs(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) CACHE_LINES.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toCacheLines(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) KILOBYTES.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toKilobytes(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) MEGABYTES.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toMegabytes(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, MemoryUnit) GIGABYTES.convert(amount, this)}.
     * @param amount the amount
     * @return the converted amount
     */
    public long toGigabytes(long amount) {
        throw new AbstractMethodError();
    }
}
