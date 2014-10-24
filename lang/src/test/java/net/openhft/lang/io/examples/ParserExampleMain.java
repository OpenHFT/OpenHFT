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

package net.openhft.lang.io.examples;

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.StopCharTesters;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Run with -verbosegc -Xmx32m
 *
 * <p>Average time was 282 nano-seconds
 */
public class ParserExampleMain {
    public static void main(String... ignored) {
        ByteBuffer wrap = ByteBuffer.allocate(1024);
        ByteBufferBytes bufferBytes = new ByteBufferBytes(wrap);
        byte[] bytes = "BAC,12.32,12.54,12.56,232443".getBytes();

        int runs = 10000000;
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            bufferBytes.clear();
            // read the next message.
            bufferBytes.write(bytes);
            bufferBytes.position(0);
            // decode message
            String word = bufferBytes.parseUTF(StopCharTesters.COMMA_STOP);
            double low = bufferBytes.parseDouble();
            double curr = bufferBytes.parseDouble();
            double high = bufferBytes.parseDouble();
            long sequence = bufferBytes.parseLong();
            if (i == 0) {
                assertEquals("BAC", word);
                assertEquals(12.32, low, 0.0);
                assertEquals(12.54, curr, 0.0);
                assertEquals(12.56, high, 0.0);
                assertEquals(232443, sequence);
            }
        }
        long time = System.nanoTime() - start;
        System.out.println("Average time was " + time / runs + " nano-seconds");
    }
}
