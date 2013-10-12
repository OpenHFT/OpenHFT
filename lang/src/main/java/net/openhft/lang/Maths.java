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

package net.openhft.lang;

/**
 * @author peter.lawrey
 */
public class Maths {
    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    public static final double WHOLE_NUMBER = 1L << 53;
    private static final long[] TENS = new long[19];

    static {
        TENS[0] = 1;
        for (int i = 1; i < TENS.length; i++)
            TENS[i] = TENS[i - 1] * 10;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it might be rounded up or
     * down. This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the
     * precision of double.
     *
     * @param d value to round
     */
    public static double round2(double d) {
        final double factor = 1e2;
        return d > WHOLE_NUMBER || d < -WHOLE_NUMBER ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it might be rounded up or
     * down. This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the
     * precision of double.
     *
     * @param d value to round
     */
    public static double round4(double d) {
        final double factor = 1e4;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it might be rounded up or
     * down. This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the
     * precision of double.
     *
     * @param d value to round
     */
    public static double round6(double d) {
        final double factor = 1e6;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it might be rounded up or
     * down. This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the
     * precision of double.
     *
     * @param d value to round
     */
    public static double round8(double d) {
        final double factor = 1e8;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    public static long power10(int n) {
        if (n < 0 || n >= TENS.length) return -1;
        return TENS[n];
    }

    public static int nextPower2(int n, int min) {
        if (n < min) return min;
        if ((n & (n - 1)) == 0) return n;
        int i = min;
        while (i < n) {
            i *= 2;
            if (i <= 0) return 1 << 30;
        }
        return i;
    }

    public static int hash(int n) {
        n ^= (n >> 21) ^ (n >> 11);
        n ^= (n >> 7) ^ (n >> 4);
        return n;
    }

    public static int hash(long n) {
        n ^= (n >> 43) ^ (n >> 21);
        n ^= (n >> 15) ^ (n >> 7);
        return (int) n;
    }

}
