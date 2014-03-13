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

import net.openhft.lang.io.ByteBufferBytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(value = Parameterized.class)
public class DirectBitSetTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int capacityInBytes = 256 / 8;
        return Arrays.asList(new Object[][]{
                {
                        new ATSDirectBitSet(new ByteBufferBytes(
                                ByteBuffer.allocate(capacityInBytes)))
                },
                {
                        new SingleThreadedDirectBitSet(new ByteBufferBytes(
                                ByteBuffer.allocate(capacityInBytes)))
                }
        });
    }

    private static final int[] INDICES = new int[]{0, 50, 100, 127, 128, 255};

    private DirectBitSet bs;

    public DirectBitSetTest(DirectBitSet bs) {
        this.bs = bs;
        assertTrue(bs.size() >= 256);
    }

    private void setIndices() {
        bs.clear();
        for (int i : INDICES) {
            bs.set(i);
        }
    }

    private void setIndicesComplement() {
        setIndices();
        bs.flip(0, bs.size());
    }

    private void assertRangeIsClear(long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bs.get(i));
        }
    }

    private void assertRangeIsClear(String message, long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(message + ", bit: " + i, bs.get(i));
        }
    }

    private void assertRangeIsSet(long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(bs.get(i));
        }
    }

    private void assertRangeIsSet(String message, long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(message + ", bit: " + i, bs.get(i));
        }
    }

    @Test
    public void testGetSetClearAndCardinality() {
        bs.clear();
        assertEquals(0, bs.cardinality());
        int c = 0;
        for (int i : INDICES) {
            c++;
            assertEquals("At index " + i, false, bs.get(i));
            bs.set(i);
            assertEquals("At index " + i, true, bs.get(i));
            assertEquals(c, bs.cardinality());
        }

        for (int i : INDICES) {
            assertEquals("At index " + i, true, bs.get(i));
            bs.clear(i);
            assertEquals("At index " + i, false, bs.get(i));
        }

        for (int i : INDICES) {
            assertEquals("At index " + i, true, bs.setIfClear(i));
            assertEquals("At index " + i, false, bs.setIfClear(i));
        }
    }

    @Test
    public void testGetLong() {
        setIndices();
        long l0 = 1L | (1L << 50);
        assertEquals(l0, bs.getLong(0));
        long l1 = (1L << (100 - 64)) | (1L << (127 - 64));
        assertEquals(l1, bs.getLong(1));
    }

    @Test
    public void testFlip() {
        bs.clear();
        for (int i : INDICES) {
            assertEquals("At index " + i, false, bs.get(i));
            bs.flip(i);
            assertEquals("At index " + i, true, bs.get(i));
            bs.flip(i);
            assertEquals("At index " + i, false, bs.get(i));
        }
    }

    @Test
    public void testNextSetBit() {
        setIndices();
        int order = 0;
        for (long i = bs.nextSetBit(0L); i >= 0; i = bs.nextSetBit(i + 1)) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bs.nextSetBit(bs.size()));

        bs.clear();
        assertEquals(-1, bs.nextSetBit(0L));
    }

    @Test
    public void testClearNextSetBit() {
        setIndices();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextSetBit(0L); i >= 0;
                i = bs.clearNextSetBit(i + 1)) {
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearNextSetBit(bs.size()));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearNextSetBit(0L));
    }

    @Test
    public void testClearNext1SetBit() {
        setIndices();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextNContinuousSetBits(0L, 1); i >= 0;
             i = bs.clearNextNContinuousSetBits(i + 1, 1)) {
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearNextNContinuousSetBits(bs.size(), 1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearNextNContinuousSetBits(0L, 1));
    }

    @Test
    public void testNextSetLong() {
        bs.clear();
        bs.set(0);
        bs.set(255);
        long[] setLongs = {0, 3};
        int order = 0;
        for (long i = bs.nextSetLong(0L); i >= 0; i = bs.nextSetLong(i + 1)) {
            assertEquals(setLongs[order], i);
            order++;
        }
        assertEquals(-1, bs.nextSetLong(bs.size() / 64));

        bs.clear();
        assertEquals(-1, bs.nextSetLong(0L));
    }

    @Test
    public void testNextClearBit() {
        setIndicesComplement();
        int order = 0;
        for (long i = bs.nextClearBit(0L); i >= 0; i = bs.nextClearBit(i + 1)) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bs.nextClearBit(bs.size()));

        bs.setAll();
        assertEquals(-1, bs.nextClearBit(0L));
    }

    @Test
    public void testSetNextClearBit() {
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextClearBit(0L); i >= 0;
             i = bs.setNextClearBit(i + 1)) {
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setNextClearBit(bs.size()));
        assertEquals(bs.size(), bs.cardinality());
        assertEquals(-1, bs.setNextClearBit(0L));
    }

    @Test
    public void testSetNext1ClearBit() {
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextNContinuousClearBits(0L, 1); i >= 0;
             i = bs.setNextNContinuousClearBits(i + 1, 1)) {
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setNextNContinuousClearBits(bs.size(), 1));
        assertEquals(bs.size(), bs.cardinality());
        assertEquals(-1, bs.setNextNContinuousClearBits(0L, 1));
    }

    @Test
    public void testNextClearLong() {
        bs.setAll();
        bs.clear(0);
        bs.clear(255);
        long[] clearLongs = {0, 3};
        int order = 0;
        for (long i = bs.nextClearLong(0L); i >= 0; i = bs.nextClearLong(i + 1)) {
            assertEquals(clearLongs[order], i);
            order++;
        }
        assertEquals(-1, bs.nextClearLong(bs.size() / 64));

        bs.setAll();
        assertEquals(-1, bs.nextClearLong(0L));
    }

    @Test
    public void testPreviousSetBit() {
        setIndices();
        int order = INDICES.length;
        for (long i = bs.size(); (i = bs.previousSetBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i);
        }
        assertEquals(-1, bs.previousSetBit(-1));

        bs.clear();
        assertEquals(-1, bs.previousSetBit(bs.size()));
    }

    @Test
    public void testClearPreviousSetBit() {
        setIndices();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.size(); (i = bs.clearPreviousSetBit(i - 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearPreviousSetBit(-1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearPreviousSetBit(bs.size()));
    }

    @Test
    public void testClearPrevious1SetBit() {
        setIndices();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.size();
                (i = bs.clearPreviousNContinuousSetBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearPreviousNContinuousSetBits(-1, 1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearPreviousNContinuousSetBits(bs.size(), 1));
    }

    @Test
    public void testPreviousSetLong() {
        bs.clear();
        bs.set(0);
        bs.set(255);
        long[] setLongs = {0, 3};
        int order = setLongs.length;
        for (long i = bs.size() / 64; (i = bs.previousSetLong(i - 1)) >= 0; ) {
            order--;
            assertEquals(setLongs[order], i);
        }
        assertEquals(-1, bs.previousSetLong(-1));

        bs.clear();
        assertEquals(-1, bs.previousSetLong(bs.size() / 64));
    }

    @Test
    public void testPreviousClearBit() {
        setIndicesComplement();
        int order = INDICES.length;
        for (long i = bs.size(); (i = bs.previousClearBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i);
        }
        assertEquals(-1, bs.previousClearBit(-1));

        bs.setAll();
        assertEquals(-1, bs.previousClearBit(bs.size()));
    }

    @Test
    public void testSetPreviousClearBit() {
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.size(); (i = bs.setPreviousClearBit(i - 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setPreviousClearBit(-1));
        assertEquals(bs.size(), bs.cardinality());
        assertEquals(-1, bs.setPreviousClearBit(bs.size()));
    }

    @Test
    public void testSetPrevious1ClearBit() {
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.size();
                (i = bs.setPreviousNContinuousClearBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setPreviousNContinuousClearBits(-1, 1));
        assertEquals(bs.size(), bs.cardinality());
        assertEquals(-1, bs.setPreviousNContinuousClearBits(bs.size(), 1));
    }

    @Test
    public void testPreviousClearLong() {
        bs.setAll();
        bs.clear(0);
        bs.clear(255);
        long[] clearLongs = {0, 3};
        int order = clearLongs.length;
        for (long i = bs.size() / 64; (i = bs.previousClearLong(i - 1)) >= 0; ) {
            order--;
            assertEquals(clearLongs[order], i);
        }
        assertEquals(-1, bs.previousClearLong(-1));

        bs.setAll();
        assertEquals(-1, bs.previousClearLong(bs.size() / 64));
    }

    @Test
    public void testSetAll() {
        bs.clear();
        bs.setAll();
        assertEquals(bs.size(), bs.cardinality());
    }

    @Test
    public void testRangeOpsWithinLongCase() {
        bs.clear();

        bs.flip(0, 0);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.flip(0, 1);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());

        bs.clear(0, 0);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());
        bs.clear(0, 1);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());

        bs.set(0, 0);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.set(0, 1);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());
    }

    @Test
    public void testRangeOpsCrossLongCase() {
        bs.clear();

        bs.flip(63, 64);
        assertEquals(true, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(1, bs.cardinality());
        bs.flip(63, 65);
        assertEquals(false, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.clear(64);
        bs.set(63, 64);
        assertEquals(true, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.set(64);
        bs.clear(63, 64);
        assertEquals(false, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.clear(64);
        bs.set(63, 65);
        assertEquals(true, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(2, bs.cardinality());

        bs.clear(63, 65);
        assertEquals(false, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(0, bs.cardinality());

    }

    @Test
    public void testRangeOpsSpanLongCase() {
        bs.clear();

        bs.set(0, bs.size());
        assertEquals(bs.size(), bs.cardinality());

        bs.clear(0, bs.size());
        assertEquals(0, bs.cardinality());

        bs.flip(0, bs.size());
        assertEquals(bs.size(), bs.cardinality());
    }

    private String m(int n) {
        return "N: " + n + ", " + bs.getClass().getSimpleName();
    }

    @Test
    public void testSetNextNContinuousClearBitsWithinLongCase() {
        long size = (bs.size() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clear();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setNextNContinuousClearBits(0L, n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n + n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clear(size - n, size);
            assertEquals(size - n, bs.setNextNContinuousClearBits(0L, n));
            assertRangeIsSet(size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.clear(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(from, n));
            assertRangeIsSet(from, to);

            bs.clear(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(0, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @Test
    public void testSetNextNContinuousClearBitsCrossLongCase() {
        if (bs instanceof ATSDirectBitSet)
            return;
        long size = bs.size();
        for (int n : new int[] {3, 7, 13, 31, 33, 63}) {
            bs.clear();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setNextNContinuousClearBits(0L, n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n + n, bs.cardinality());
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[] {2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.setAll();
            long from = lastBound - (n / 2);
            long to = from + n;
            bs.clear(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(0L, n));
            assertRangeIsSet(from, to);

            bs.clear(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(from, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @Test
    public void testClearNextNContinuousSetBitsWithinLongCase() {
        long size = (bs.size() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearNextNContinuousSetBits(0L, n));
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(cardinality - (i * n + n), bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clear();
            bs.set(size - n, size);
            assertEquals(size - n, bs.clearNextNContinuousSetBits(0L, n));
            assertRangeIsClear(size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.set(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n));
            assertRangeIsClear(from, to);

            bs.set(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(0, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @Test
    public void testClearNextNContinuousSetBitsCrossLongCase() {
        if (bs instanceof ATSDirectBitSet)
            return;
        long size = bs.size();
        for (int n : new int[] {3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearNextNContinuousSetBits(0L, n));
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[] {2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clear();
            long from = lastBound - (n / 2);
            long to = from + n;
            bs.set(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(0L, n));
            assertRangeIsClear(from, to);

            bs.set(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @Test
    public void testSetPreviousNContinuousClearBitsWithinLongCase() {
        long size = (bs.size() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clear();
            long cardinality = 0;
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setPreviousNContinuousClearBits(size, n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(cardinality += n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clear(0, n);
            assertEquals(0, bs.setPreviousNContinuousClearBits(bs.size(), n));
            assertRangeIsSet(0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.clear(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n));
            assertRangeIsSet(from, to);

            bs.clear(from, to);
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(bs.size(), n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @Test
    public void testSetPreviousNContinuousClearBitsCrossLongCase() {
        if (bs instanceof ATSDirectBitSet)
            return;
        long size = bs.size();
        for (int n : new int[] {3, 7, 13, 31, 33, 63}) {
            bs.clear();
            long cardinality = 0;
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsClear(from, from + n);
                assertEquals(m(n), from, bs.setPreviousNContinuousClearBits(size, n));
                assertRangeIsSet(from, from + n);
                assertEquals(cardinality += n, bs.cardinality());
            }
        }
        for (int n : new int[] {2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.setAll();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.clear(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(size, n));
            assertRangeIsSet(from, to);

            bs.clear(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @Test
    public void testClearPreviousNContinuousSetBitsWithinLongCase() {
        long size = (bs.size() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearPreviousNContinuousSetBits(size, n));
                assertRangeIsClear(m(n), i * n, i * n + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clear();
            bs.set(0, n);
            assertEquals(0, bs.clearPreviousNContinuousSetBits(bs.size(), n));
            assertRangeIsClear(0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.set(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n));
            assertRangeIsClear(from, to);

            bs.set(from, to);
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(bs.size(), n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @Test
    public void testClearPreviousNContinuousSetBitsCrossLongCase() {
        if (bs instanceof ATSDirectBitSet)
            return;
        long size = bs.size();
        for (int n : new int[] {3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsSet(from, from + n);
                assertEquals(m(n), from, bs.clearPreviousNContinuousSetBits(size, n));
                assertRangeIsClear(from, from + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        for (int n : new int[] {2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clear();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.set(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(size, n));
            assertRangeIsClear(from, to);

            bs.set(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.size(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetNegative() {
        bs.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetOverCapacity() {
        bs.get(bs.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNegative() {
        bs.set(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetOverCapacity() {
        bs.set(bs.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetIfClearNegative() {
        bs.setIfClear(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetIfClearOverCapacity() {
        bs.setIfClear(bs.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipNegative() {
        bs.flip(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipOverCapacity() {
        bs.flip(bs.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetLongNegative() {
        bs.getLong(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetLongOverCapacity() {
        bs.getLong((bs.size() + 63) / 64);
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextSetBit() {
        bs.nextSetBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextSetLong() {
        bs.nextSetLong(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextClearBit() {
        bs.nextClearBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextClearLong() {
        bs.nextClearLong(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousSetBit() {
        bs.previousSetBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousSetLong() {
        bs.previousSetLong(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousClearBit() {
        bs.previousClearBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousClearLong() {
        bs.previousClearLong(-2);
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearNextSetBit() {
        bs.clearNextSetBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearNextNContinuousSetBits() {
        bs.clearNextNContinuousSetBits(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNextClearBit() {
        bs.setNextClearBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNextNContinuousClearBits() {
        bs.setNextNContinuousClearBits(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearPreviousSetBit() {
        bs.clearPreviousSetBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearPreviousNContinuousSetBit() {
        bs.clearPreviousNContinuousSetBits(-2, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetPreviousClearBit() {
        bs.setPreviousClearBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetPreviousNContinuousClearBit() {
        bs.setPreviousNContinuousClearBits(-2, 2);
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeFromNegative() {
        bs.set(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeFromOverTo() {
        bs.set(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeToOverCapacity() {
        bs.set(0, bs.size() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeFromNegative() {
        bs.clear(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeFromOverTo() {
        bs.clear(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeToOverCapacity() {
        bs.clear(0, bs.size() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeFromNegative() {
        bs.flip(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeFromOverTo() {
        bs.flip(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeToOverCapacity() {
        bs.flip(0, bs.size() + 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIaeClearNextNContinuousSetBits() {
        bs.clearNextNContinuousSetBits(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeSetNextNContinuousClearBits() {
        bs.setNextNContinuousClearBits(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeClearPreviousNContinuousSetBits() {
        bs.clearPreviousNContinuousSetBits(bs.size(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeSetPreviousNContinuousClearBits() {
        bs.setPreviousNContinuousClearBits(bs.size(), 0);
    }
}
