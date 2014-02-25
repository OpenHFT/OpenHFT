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

import net.openhft.lang.io.ByteBufferBytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
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
}
