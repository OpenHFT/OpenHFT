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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;

import java.math.BigDecimal;

/**
 * @author peter.lawrey
 */
@SuppressWarnings({"CompareToUsesNonFinalVariable", "NonFinalFieldReferenceInEquals", "NonFinalFieldReferencedInHashCode"})
public class MutableDecimal extends Number implements Comparable<MutableDecimal> {
    private static final double[] TENS = new double[16];

    static {
        TENS[0] = 1;
        for (int i = 1; i < TENS.length; i++)
            TENS[i] = 10 * TENS[i - 1];
    }

    private long value;
    private int scale;

    public MutableDecimal() {
        this(0, Integer.MIN_VALUE);
    }

    public MutableDecimal(long value, int scale) {
        this.value = value;
        this.scale = scale;
    }

    public MutableDecimal(long value) {
        this(value, 0);
    }

    public MutableDecimal(double d, int precision) {
        set(d, precision);
    }

    void set(double d, int precision) {
        while (d > Long.MAX_VALUE) {
            d /= 10;
            precision++;
        }
        value = Math.round(d);
        this.scale = precision;
    }

    public void set(long value, int scale) {
        this.value = value;
        this.scale = scale;
    }

    public long value() {
        return value;
    }

    int scale() {
        return scale;
    }

    public void clear() {
        scale = Integer.MIN_VALUE;
    }

    public boolean isSet() {
        return scale > Integer.MIN_VALUE;
    }

    @Override
    public int hashCode() {
        int hash = (int) value;
        hash ^= value >>> 32;
        hash ^= scale * 37;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MutableDecimal))
            return false;
        MutableDecimal md = (MutableDecimal) obj;
        return value == md.value && scale == md.scale;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(20);
        toString(sb);
        return sb.toString();
    }

    public void toString(@NotNull StringBuilder sb) {
        if (scale == Integer.MIN_VALUE) {
            sb.append("not set");
            return;
        }
        if (value == 0 && scale <= 0) {
            sb.append('0');
            return;
        }

        boolean neg = false;
        long v = value;
        int s = scale;
        if (v < 0) {
            v = -value;
            neg = true;
        }
        for (int s2 = scale; s2 < 0; s2++)
            sb.append('0');
        while (v != 0 || s >= 0) {
            int digit = (int) (v % 10);
            // MIN_VALUE
            if (digit < 0) {
                digit = 8;
                v = (v >>> 1) / 5;

            } else {
                v /= 10;
            }
            sb.append((char) ('0' + digit));
            if (--s == 0)
                sb.append('.');
        }
        if (neg)
            sb.append('-');
        sb.reverse();
    }

    @NotNull
    @Override
    public MutableDecimal clone() throws CloneNotSupportedException {
        return (MutableDecimal) super.clone();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    @Override
    public long longValue() {
        if (scale == 0)
            return value;
        if (scale > 0 && scale < TENS.length)
            return value / (long) TENS[scale];
        if (scale < 0 && -scale < TENS.length)
            return value * (long) TENS[-scale];
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    public double doubleValue() {
        if (scale == 0)
            return value;
        return scale <= 0 ? value * tens(-scale) : value / tens(scale);
    }

    private static double tens(int scale) {
        return scale < TENS.length ? TENS[scale] : Math.pow(10, scale);
    }

    @Override
    public int compareTo(@NotNull MutableDecimal o) {
        long value = this.value, ovalue = o.value;
        if (scale == o.scale)
            return longCompareTo(value, ovalue);
        if (value == 0 && ovalue == 0)
            return 0;
        double d = doubleValue(), od = o.doubleValue();
        double err = (Math.abs(d) + Math.abs(od)) / 1e15;
        if (d + err < od) return -1;
        if (d > od + err) return +1;
        // fallback.
        return BigDecimal.valueOf(value, scale).compareTo(BigDecimal.valueOf(ovalue, o.scale()));
    }

    private static int longCompareTo(long value, long ovalue) {
        return value < ovalue ? -1 : value > ovalue ? +1 : 0;
    }
}
