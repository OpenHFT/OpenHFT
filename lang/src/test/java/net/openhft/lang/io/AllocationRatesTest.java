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

package net.openhft.lang.io;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * User: peter
 * Date: 24/12/13
 * Time: 19:43
 */
public class AllocationRatesTest {
    static final int BUFFER_SIZE = 64 * 1024;
    static final int ALLOCATIONS = 10000;

    @Test
    public void compareAllocationRates() {
        for (int i = 0; i < 3; i++) {
            long timeHBB = timeHeapByteBufferAllocations();
            long timeDBB = timeDirectByteBufferAllocations();
            long timeDS = timeDirectStoreAllocations();
            System.out.printf("buffers %d KB took an average of %,d ns for heap ByteBuffer, %,d ns for direct ByteBuffer and %,d for DirectStore%n",
                    BUFFER_SIZE / 1024, timeHBB / ALLOCATIONS, timeDBB / ALLOCATIONS, timeDS / ALLOCATIONS
            );
        }
    }

    private long timeHeapByteBufferAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i++) {
            ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
        }
        return System.nanoTime() - start;
    }

    private long timeDirectByteBufferAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i++) {
            ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
        return System.nanoTime() - start;
    }

    private long timeDirectStoreAllocations() {
        long start = System.nanoTime();
        for (int i = 0; i < ALLOCATIONS; i++) {
            DirectStore ds = DirectStore.allocateLazy(BUFFER_SIZE);
            ds.free();
        }
        return System.nanoTime() - start;
    }
}
