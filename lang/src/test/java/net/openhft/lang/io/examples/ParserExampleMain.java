/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.io.examples;

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
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
        Bytes bufferBytes = ByteBufferBytes.wrap(wrap);
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
