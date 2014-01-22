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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: peter
 * Date: 22/12/13
 * Time: 11:05
 *
 * Toggled 10,000,128 times with an average delay of 50 ns
 */
public class LockingViaMMapMain {
    static int RECORDS = Integer.getInteger("records", 128);
    static int RECORD_SIZE = Integer.getInteger("record_size", 64); // cache line size
    static int WARMUP = Integer.getInteger("warmup", RECORDS * 100);
    static int RUNS = Integer.getInteger("runs", 5 * 1000 * 1000);

    // offsets
    static int LOCK = 0;
    static int FLAG = 8;
    static int LENGTH = 16;

    public static void main(String... args) throws IOException, InterruptedException {
        boolean toggleTo = Boolean.parseBoolean(args[0]);
        File tmpFile = new File(System.getProperty("java.io.tmpdir"), "lock-test.dat");
        FileChannel fc = new RandomAccessFile(tmpFile, "rw").getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, RECORDS * RECORD_SIZE);
        ByteBufferBytes bytes = new ByteBufferBytes(mbb.order(ByteOrder.nativeOrder()));
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
                    bytes.busyLockLong(recordOffset + LOCK);
                    try {
                        boolean flag = bytes.readBoolean(recordOffset + FLAG);
                        if (flag != toggleTo) {
                            bytes.writeBoolean(recordOffset + FLAG, toggleTo);
                            break;
                        }
                    } finally {
                        bytes.unlockLong(recordOffset + LOCK);
                    }
                    if (t % 100 == 0)
                        System.out.println("waiting for " + j
                                + " pid " + (int) bytes.readLong(recordOffset + LOCK)
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
                toggles, 2 * time / toggles);
        fc.close();
        tmpFile.deleteOnExit();
    }
}
