package net.openhft.chronicle.core;

public enum Maths {
    ;
    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    private static final double WHOLE_NUMBER = 1L << 53;

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it might be rounded up or
     * down. This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the
     * precision of double.
     *
     * @param d value to round
     * @return rounded value
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
     * @return rounded value
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
     * @return rounded value
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
     * @return rounded value
     */
    public static double round8(double d) {
        final double factor = 1e8;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

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


    public static int intLog2(long num) {
        long l = Double.doubleToRawLongBits((double) num);
        return (int) ((l >> 52) - 1023L);
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

    public static int toUInt31(long x) {
        if (x < 0 || x > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Unsigned Int 31-bit " + x + " out of range");
        return (int) x;
    }

    public static long toUInt32(long x) {
        if (x < 0 || x > 1L << 32) throw new IllegalArgumentException("Unsigned Int " + x + " out of range");
        return x;
    }

}
