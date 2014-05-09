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

import net.openhft.affinity.AffinitySupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: peter
 * Date: 22/12/13
 * Time: 11:05
 *
 * <p>Toggled 10,000,128 times with an average delay of 20 ns on i7-4700
 * Toggled 10,000,128 times with an average delay of 14 ns on i7-3970X
 */
public class LockingViaMMapWithThreadIdMain {
    static int RECORDS = Integer.getInteger("records", 256);
    static int RECORD_SIZE = Integer.getInteger("record_size", 64); // double cache line size
    static int WARMUP = Integer.getInteger("warmup", RECORDS * 50);
    static int RUNS = Integer.getInteger("runs", 50 * 1000 * 1000);

    // offsets
    static int LOCK = 0;
    static int FLAG = 8;
    static int LENGTH = 16;

    public static void main(String... args) throws IOException, InterruptedException {
        boolean toggleTo = Boolean.parseBoolean(args[0]);
        File tmpFile = new File(System.getProperty("java.io.tmpdir"), "lock-test-tid.dat");
        FileChannel fc = new RandomAccessFile(tmpFile, "rw").getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, RECORDS * RECORD_SIZE);
        // set the the Thread.getId() to match the process thread id
        // this way the getId() can be used across processes..
        AffinitySupport.setThreadId();
        AffinitySupport.setAffinity(toggleTo ? 1 << 3 : 1 << 2);
        ByteBufferBytes bytes = new ByteBufferBytes(mbb);
        bytes.setCurrentThread();

        long start = 0;
        for (int i = -WARMUP / RECORDS; i < (RUNS + RECORDS - 1) / RECORDS; i++) {
            if (i == 0) {
                start = System.nanoTime();
                System.out.println("Started");
            }

            for (int j = 0; j < RECORDS; j++) {
                int recordOffset = j * RECORD_SIZE;
                for (int t = 1; t < 10000; t++) {
                    if (t == 0)
                        if (i >= 0) {
                            throw new AssertionError("Didn't toggle in time !??");
                        } else {
                            Thread.sleep(200);
                        }
                    bytes.busyLockInt(recordOffset + LOCK);
                    try {
                        boolean flag = bytes.readBoolean(recordOffset + FLAG);
                        if (flag != toggleTo) {
                            bytes.writeBoolean(recordOffset + FLAG, toggleTo);
                            break;
                        }
                    } finally {
                        bytes.unlockInt(recordOffset + LOCK);
                    }
                    if (t % 100 == 0)
                        System.out.println("waiting for " + j
                                + " pid " + (bytes.readInt(recordOffset + LOCK) & (-1 >>> 8))
                                + " is " + bytes.readBoolean(recordOffset + FLAG));
                    if (t > 100)
                        if (t > 200)
                            Thread.sleep(1);
                        else
                            Thread.yield();
                }
            }
        }
        long time = System.nanoTime() - start;
        final int toggles = (RUNS + RECORDS - 1) / RECORDS * RECORDS * 2; // one for each of two processes.
        System.out.printf("Toggled %,d times with an average delay of %,d ns%n",
                toggles, time / toggles);
        fc.close();
        tmpFile.deleteOnExit();
    }
}
