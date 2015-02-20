package net.openhft.core;

public enum Maths {
    ;

    public static int nextPower2(int n, int min) {
        return (int) Math.min(1 << 30, nextPower2((long) n, (long) min));
    }

    public static long nextPower2(long n, long min) {
        if (!isPowerOf2(min))
            throw new IllegalArgumentException();
        if (n < min) return min;
        if (isPowerOf2(n))
            return n;
        long i = min;
        while (i < n) {
            i *= 2;
            if (i <= 0) return 1L << 62;
        }
        return i;
    }

    public static boolean isPowerOf2(long n) {
        return n != 0 && (n & (n - 1L)) == 0L;
    }

    public static int hash(int n) {
        n ^= (n >>> 21) - (n >>> 11);
        n ^= (n >>> 7) + (n >>> 4);
        return n;
    }

    public static long hash(long n) {
        n ^= (n >>> 41) - (n >>> 21);
        n ^= (n >>> 15) + (n >>> 7);
        return n;
    }

    public static long hash(CharSequence cs) {
        long hash = 0;
        for (int i = 0; i < cs.length(); i++)
            hash = hash * 131 + cs.charAt(i);
        return hash;
    }

    public static int toInt32(long x, String msg) {
        if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE)
            throw new IllegalArgumentException(String.format(msg, x));
        return (int) x;
    }

    public static byte toInt8(long x) {
        if (x < Byte.MIN_VALUE || x > Byte.MAX_VALUE)
            throw new IllegalArgumentException("Byte " + x + " out of range");
        return (byte) x;
    }

    public static short toUInt8(long x) {
        if (x < 0 || x > 1 << 8) throw new IllegalArgumentException("Unsigned Byte " + x + " out of range");
        return (short) x;
    }

    public static short toInt16(long x) {
        if (x < Short.MIN_VALUE || x > Short.MAX_VALUE)
            throw new IllegalArgumentException("Short " + x + " out of range");
        return (byte) x;
    }

    public static int toUInt16(long x) {
        if (x < 0 || x > 1 << 16) throw new IllegalArgumentException("Unsigned Short " + x + " out of range");
        return (int) x;
    }

    public static int toInt32(long x) {
        if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Int " + x + " out of range");
        return (int) x;
    }

    public static long toUInt32(long x) {
        if (x < 0 || x > 1L << 32) throw new IllegalArgumentException("Unsigned Int " + x + " out of range");
        return x;
    }

}
