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
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class DirectBitSetTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int capacityInBytes = 256 / 8;
        return Arrays.asList(new Object[][] { {
                new ATSDirectBitSet(new ByteBufferBytes(
                        ByteBuffer.allocate(capacityInBytes)
                                .order(ByteOrder.nativeOrder())))
        } });
    }

    private static final int[] INDICES = new int[] {0, 50, 100, 127, 128, 255};

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
        bs.setAll();
        for (int i : INDICES) {
            bs.clear(i);
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
    public void testIoobeNextClearBit() {
        bs.nextClearBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousSetBit() {
        bs.previousSetBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousClearBit() {
        bs.previousClearBit(-2);
    }
}
