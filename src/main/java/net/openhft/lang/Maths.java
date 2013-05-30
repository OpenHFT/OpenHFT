package net.openhft.lang;

/**
 * @author peter.lawrey
 */
public class Maths {
    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    public static final double WHOLE_NUMBER = 1L << 53;

    /**
     * Performs a round which is accurate to within 1 ulp.
     * i.e. for values very close to 0.5 it might be rounded up or down.
     * This is a pragmatic choice for performance reasons as it is assumed you are not working on the edge of the precision of double.
     */
    public static double round2(double d) {
        final double factor = 1e2;
        return d > WHOLE_NUMBER || d < -WHOLE_NUMBER ?
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static double round4(double d) {
        final double factor = 1e4;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ?
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static double round6(double d) {
        final double factor = 1e6;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ?
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static double round8(double d) {
        final double factor = 1e8;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ?
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    private static final long[] TENS = new long[19];

    static {
        TENS[0] = 1;
        for (int i = 1; i < TENS.length; i++)
            TENS[i] = TENS[i - 1] * 10;
    }

    public static long power10(int n) {
        if (n < 0 || n >= TENS.length) return -1;
        return TENS[n];
    }
}
