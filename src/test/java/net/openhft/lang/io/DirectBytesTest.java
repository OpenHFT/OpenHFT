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

import net.openhft.lang.affinity.PosixJNAAffinity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author peter.lawrey
 */
public class DirectBytesTest {
    static final boolean WITH_BINDING;

    static {
        boolean binding = false;

        if (Runtime.getRuntime().availableProcessors() >= 12) {
            try {
                PosixJNAAffinity.INSTANCE.getcpu();
                binding = true;
                System.out.println("binding: true");
            } catch (Throwable ignored) {
            }
        }
        WITH_BINDING = binding;
    }

    @Test
    public void testAllocate() throws Exception {
        long size = 1L << 31;
        DirectStore store = DirectStore.allocate(size);
        DirectBytes slice = store.createSlice();
        slice.writeLong(0, size);
        slice.writeLong(size - 8, size);
        store.free();
    }

    @Test
    public void testLocking() {
        long start = System.nanoTime();
        // a page
        final DirectStore store1 = DirectStore.allocate(1 << 12);
        final int lockCount = 200 * 1000 * 1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (WITH_BINDING) PosixJNAAffinity.INSTANCE.setAffinity(1L << 11);
                manyToggles(store1, lockCount, 1, 0);
            }
        }).start();

        if (WITH_BINDING) PosixJNAAffinity.INSTANCE.setAffinity(1L << 5);
        manyToggles(store1, lockCount, 0, 1);

        store1.free();
        long time = System.nanoTime() - start;
        System.out.printf("Contended lock rate was %,d per second%n", (int) (lockCount * 2 * 1e9 / time));
    }

    private void manyToggles(DirectStore store1, int lockCount, int from, int to) {
        long id = Thread.currentThread().getId();
        assertEquals(0, id >>> 24);
        System.out.println("Thread " + id);

        DirectBytes slice1 = store1.createSlice();

        int records = 8;
        for (int i = 0; i < lockCount; i += records) {
            for (long j = 0; j < records * 64; j += 64) {
                slice1.positionAndSize(j, 64);
                assertTrue(
                        slice1.tryLockNanosInt(0L, 5 * 1000 * 1000));
                int toggle1 = slice1.readInt(4);
                if (toggle1 == from) {
                    slice1.writeInt(4L, to);
                } else {
                    i--;
                }
                slice1.unlockInt(0L);
            }
        }
    }

    @Test
    public void testLocking2() throws Exception {
        // a page
        final DirectStore store1 = DirectStore.allocate(1 << 12);
        final DirectStore store2 = DirectStore.allocate(1 << 12);
        final int lockCount = 10 * 1000000;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                long id = Thread.currentThread().getId();
                System.out.println("Thread " + id);
                assertEquals(0, id >>> 24);
                int expected = (1 << 24) | (int) id;
                try {
                    DirectBytes slice1 = store1.createSlice();
                    DirectBytes slice2 = store2.createSlice();

                    for (int i = 0; i < lockCount; i += 2) {
                        slice1.busyLockInt(0);
                        slice2.busyLockInt(0);
                        int lockValue1 = slice1.readInt(0);
                        if (lockValue1 != expected)
                            assertEquals(expected, lockValue1);
                        int lockValue2 = slice2.readInt(0);
                        if (lockValue2 != expected)
                            assertEquals(expected, lockValue2);
                        int toggle1 = slice1.readInt(4);
                        if (toggle1 == 1) {
                            slice1.writeInt(4, 0);
//                            if (i % 10000== 0)
//                            System.out.println("t: " + i);
                        } else {
                            i--;
                        }
                        int toggle2 = slice2.readInt(4);
                        if (toggle2 == 1) {
                            slice2.writeInt(4, 0);
//                            if (i % 10000== 0)
//                            System.out.println("t: " + i);
                        } else {
                            i--;
                        }
                        int lockValue1A = slice1.readInt(0);
                        int lockValue2A = slice1.readInt(0);
                        try {
                            slice2.unlockInt(0);
                            slice1.unlockInt(0);
                        } catch (IllegalStateException e) {
                            int lockValue1B = slice1.readInt(0);
                            int lockValue2B = slice2.readInt(0);
                            System.err.println("i= " + i +
                                    " lock: " + Integer.toHexString(lockValue1A) + " / " + Integer.toHexString(lockValue2A) +
                                    " lock: " + Integer.toHexString(lockValue1B) + " / " + Integer.toHexString(lockValue2B));
                            throw e;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        long id = Thread.currentThread().getId();
        assertEquals(0, id >>> 24);
        int expected = (1 << 24) | (int) id;
        System.out.println("Thread " + id);

        DirectBytes slice1 = store1.createSlice();
        DirectBytes slice2 = store2.createSlice();
        for (int i = 0; i < lockCount; i += 2) {
            slice1.busyLockInt(0);
            slice2.busyLockInt(0);
            int lockValue1 = slice1.readInt(0);
            if (lockValue1 != expected)
                assertEquals(expected, lockValue1);
            int lockValue2 = slice2.readInt(0);
            if (lockValue2 != expected)
                assertEquals(expected, lockValue2);
            int toggle1 = slice1.readInt(4);
            if (toggle1 == 0) {
                slice1.writeInt(4, 1);
//                            if (i % 10000== 0)
//                            System.out.println("t: " + i);
            } else {
                i--;
            }
            int toggle2 = slice2.readInt(4);
            if (toggle2 == 0) {
                slice2.writeInt(4, 1);
//                            if (i % 10000== 0)
//                            System.out.println("t: " + i);
            } else {
                i--;
            }
            int lockValue1A = slice1.readInt(0);
            int lockValue2A = slice1.readInt(0);
            try {
                slice2.unlockInt(0);
                slice1.unlockInt(0);
            } catch (IllegalStateException e) {
                int lockValue1B = slice1.readInt(0);
                int lockValue2B = slice2.readInt(0);
                System.err.println("i= " + i +
                        " lock: " + Integer.toHexString(lockValue1A) + " / " + Integer.toHexString(lockValue2A) +
                        " lock: " + Integer.toHexString(lockValue1B) + " / " + Integer.toHexString(lockValue2B));
                throw e;
            }
        }

        store1.free();
        store2.free();
    }

}
