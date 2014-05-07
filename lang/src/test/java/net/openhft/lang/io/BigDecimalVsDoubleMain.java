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

import java.math.BigDecimal;

public class BigDecimalVsDoubleMain {

    public static final String[] NUMBER = {"1000000", "1.1", "1.23456", "12345.67890"};
    public static final Bytes[] IN_BYTES = new Bytes[NUMBER.length];
    public static final Bytes OUT_BYTES;

    static {
        DirectStore store = new DirectStore((NUMBER.length + 1) * 16);
        for (int i = 0; i < NUMBER.length; i++) {
            IN_BYTES[i] = store.createSlice((i + 1) * 16, 16);
            IN_BYTES[i].append(NUMBER[i]);
        }
        OUT_BYTES = store.createSlice(0, 16);
    }

    static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        int runs = 5000;

        for (int t = 0; t < 5; t++) {
            long timeD = 0;
            long timeBD = 0;
            long timeB = 0;
            if (t == 0)
                System.out.println("Warming up");
            else if (t == 1)
                System.out.println("Cold code");

            int r = t == 0 ? 20000 : runs;
            for (int i = 0; i < r; i += 3) {
                count++;
                if (count >= NUMBER.length) count = 0;

                if (t > 0)
                    Thread.sleep(1);
                timeB += testDoubleWithBytes();
                timeBD += testBigDecimalWithString();
                timeD += testDoubleWithString();
                if (t > 0)
                    Thread.sleep(1);
                timeD += testDoubleWithString();
                timeB += testDoubleWithBytes();
                timeBD += testBigDecimalWithString();
                if (t > 0)
                    Thread.sleep(1);
                timeBD += testBigDecimalWithString();
                timeD += testDoubleWithString();
                timeB += testDoubleWithBytes();
            }
            System.out.printf("double took %.1f us, BigDecimal took %.1f, double with Bytes took %.1f%n",
                    timeD / 1e3 / r, timeBD / 1e3 / r, timeB / 1e3 / r);
        }
    }

    static volatile double saved;
    static volatile String savedStr;

    public static long testDoubleWithString() {
        long start = System.nanoTime();
        saved = Double.parseDouble(NUMBER[count]);
        savedStr = Double.toString(saved);
        return System.nanoTime() - start;
    }

    public static long testDoubleWithBytes() {
        IN_BYTES[count].position(0);
        OUT_BYTES.position(0);

        long start = System.nanoTime();
        saved = IN_BYTES[count].parseDouble();
        OUT_BYTES.append(saved);
        return System.nanoTime() - start;
    }

    static volatile BigDecimal savedBD;

    public static long testBigDecimalWithString() {
        long start = System.nanoTime();
        savedBD = new BigDecimal(NUMBER[count]);
        savedStr = savedBD.toString();
        return System.nanoTime() - start;
    }
}
