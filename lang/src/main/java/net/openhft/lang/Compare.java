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

package net.openhft.lang;

/**
 * A generic Compare class for many types.  It can be sub-classed so you can give types a different behaviour.
 */
public abstract class Compare {

    private static final long NULL_HASHCODE = Long.MIN_VALUE;

    public static boolean isEqual(boolean a, boolean b) {
        return a == b;
    }

    private static long calcLongHashCode(boolean a) {
        return a ? 1 : 0;
    }

    public static boolean isEqual(Boolean a, Boolean b) {
        return a == null ? b == null : b != null && a.booleanValue() == b.booleanValue();
    }

    public static long calcLongHashCode(Boolean a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.booleanValue());
    }

    public static boolean isEqual(byte a, byte b) {
        return a == b;
    }

    private static long calcLongHashCode(byte a) {
        return a;
    }

    public static boolean isEqual(Byte a, Byte b) {
        return a == null ? b == null : b != null && a.byteValue() == b.byteValue();
    }

    public static long calcLongHashCode(Byte a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.byteValue());
    }

    public static boolean isEqual(char a, char b) {
        return a == b;
    }

    private static long calcLongHashCode(char a) {
        return a;
    }

    public static boolean isEqual(Character a, Character b) {
        return a == null ? b == null : b != null && a.charValue() == b.charValue();
    }

    public static long calcLongHashCode(Character a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.charValue());
    }

    public static boolean isEqual(short a, short b) {
        return a == b;
    }

    private static long calcLongHashCode(short a) {
        return a;
    }

    public static boolean isEqual(Short a, Short b) {
        return a == null ? b == null : b != null && a.shortValue() == b.shortValue();
    }

    public static long calcLongHashCode(Short a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.shortValue());
    }

    public static boolean isEqual(int a, int b) {
        return a == b;
    }

    private static long calcLongHashCode(int a) {
        return a;
    }

    public static long calcLongHashCode(Integer a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.intValue());
    }

    public static boolean isEqual(Integer a, Integer b) {
        return a == null ? b == null : b != null && a.intValue() == b.intValue();
    }

    public static boolean isEqual(long a, long b) {
        return a == b;
    }

    private static long calcLongHashCode(long a) {
        return a;
    }

    public static boolean isEqual(Long a, Long b) {
        return a == null ? b == null : b != null && a.longValue() == b.longValue();
    }

    public static long calcLongHashCode(Long a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.longValue());
    }

    private static boolean isEqual(float a, float b) {
        return Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b);
    }

    private static long calcLongHashCode(float a) {
        return Float.floatToRawIntBits(a);
    }

    public static boolean isEqual(Float a, Float b) {
        return a == null ? b == null : b != null && isEqual(a.floatValue(), b.floatValue());
    }

    public static long calcLongHashCode(Float a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.floatValue());
    }

    private static boolean isEqual(double a, double b) {
        return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
    }

    private static long calcLongHashCode(double a) {
        return Double.doubleToRawLongBits(a);
    }

    public static boolean isEqual(Double a, Double b) {
        return a == null ? b == null : b != null && isEqual(a.doubleValue(), b.doubleValue());
    }

    public static long calcLongHashCode(Double a) {
        return a == null ? NULL_HASHCODE : calcLongHashCode(a.doubleValue());
    }

    public static <T> boolean isEqual(T a, T b) {
        return a == null ? b == null : b != null && a.equals(b);
    }

    private static long calcLongHashCode(LongHashable t) {
        return t.longHashCode();
    }

    public static long calcLongHashCode(Object t) {
        return t == null ? NULL_HASHCODE :
                t instanceof LongHashable ? calcLongHashCode((LongHashable) t) :
                        t instanceof CharSequence ? calcLongHashCode((CharSequence) t) :
                                t.hashCode();
    }

    public static long calcLongHashCode(CharSequence s) {
        long hash = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            hash = 57 * hash + s.charAt(i);
        }
        return hash;
    }
}
