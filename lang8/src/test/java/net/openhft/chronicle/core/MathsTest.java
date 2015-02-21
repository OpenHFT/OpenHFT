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

package net.openhft.chronicle.core;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 10:31
 */
public class MathsTest {
    @Test
    public void testIntLog2() {
        for (int i = 0; i < 63; i++) {
            long l = 1L << i;
            assertEquals(i, Maths.intLog2(l));
        }
    }

    @Test
    public void testRounding() {
        Random rand = new Random(1);
        for (int i = 0; i < 1000; i++) {
            double d = Math.pow(1e18, rand.nextDouble()) / 1e6;
            BigDecimal bd = new BigDecimal(d);
            assertEquals(bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round2(d), 5e-2);
            assertEquals(bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round4(d), 5e-4);
            assertEquals(bd.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round6(d), 5e-6);
            if (d < 1e8)
                assertEquals(bd.setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round8(d), 5e-8);
        }
    }
}
