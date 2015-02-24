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

package net.openhft.chronicle.bytes;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter Date: 24/12/13 Time: 19:43
 */
/*
buffers 128 KB took an average of 18,441 ns for heap ByteBuffer, 33,683 ns for direct ByteBuffer and 1,761 for DirectStore
buffers 128 KB took an average of 13,062 ns for heap ByteBuffer, 17,855 ns for direct ByteBuffer and 903 for DirectStore
buffers 128 KB took an average of 12,809 ns for heap ByteBuffer, 21,602 ns for direct ByteBuffer and 922 for DirectStore
buffers 128 KB took an average of 10,768 ns for heap ByteBuffer, 21,444 ns for direct ByteBuffer and 894 for DirectStore
buffers 128 KB took an average of 8,739 ns for heap ByteBuffer, 22,684 ns for direct ByteBuffer and 890 for DirectStore
 */
public class AllocationRatesTest {
    static final int BUFFER_SIZE = 128 * 1024;
    static final int ALLOCATIONS = 10000;
    public static final int BATCH = 10;

    @Test
    public void compareAllocationRates() {
        for (int i = 0; i < 5; i++) {
            long timeHBB = timeHeapByteBufferAllocations();
            long timeDBB = timeDirectByteBufferAllocations();
            long timeDS = timeDirectStoreAllocations();
            System.out.printf("buffers %d KB took an average of %,d ns for heap ByteBuffer, %,d ns for direct ByteBuffer and %,d for DirectStore%n",
                    BUFFER_SIZE / 1024, timeHBB / ALLOCATIONS, timeDBB / ALLOCATIONS, timeDS / ALLOCATIONS);
        }
    }

    private long timeHeapByteBufferAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i += BATCH) {
            ByteBuffer[] bb = new ByteBuffer[BATCH];
            for (int j = 0; j < BATCH; j++)
                bb[j] = ByteBuffer.allocate(BUFFER_SIZE);
        }
        return System.nanoTime() - start;
    }

    private long timeDirectByteBufferAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i += BATCH) {
            ByteBuffer[] bb = new ByteBuffer[BATCH];
            for (int j = 0; j < BATCH; j++)
                bb[j] = ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
        return System.nanoTime() - start;
    }

    private long timeDirectStoreAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i += BATCH) {
            NativeStore[] ds = new NativeStore[BATCH];
            for (int j = 0; j < BATCH; j++)
                ds[j] = NativeStore.lazyNativeStore(BUFFER_SIZE);
            for (int j = 0; j < BATCH; j++) {
                ds[j].release();
                assertEquals(0, ds[j].refCount());
            }
        }
        return System.nanoTime() - start;
    }
}
