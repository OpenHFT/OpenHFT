package net.openhft.lang.data.attic;

/**
 * Created by peter on 1/10/15.
 */
public enum EncodeMode {
    /**
     * Write numbers fixed length and String UTF-8 encoded.
     */
    LITERAL,
    /**
     * use stop bit encoding where possible.
     */
    COMPACT,
    /**
     * Compress allowing a 1 ulp loss for float and double
     */
    COMPRESS {
        @Override
        public boolean compare(float a, float b) {
            return Math.abs(Float.floatToIntBits(a) - Float.floatToIntBits(b)) <= 1;
        }

        @Override
        public boolean compare(double a, double b) {
            return Math.abs(Double.doubleToLongBits(a) - Double.doubleToLongBits(b)) <= 1;
        }
    },
    /**
     * Compress allowing a rounding to 6 decimal places
     */
    ROUND6 {
        @Override
        public boolean compare(float a, float b) {
            return Math.abs(a - b) < 0.5e6f;
        }

        @Override
        public boolean compare(double a, double b) {
            return Math.abs(a - b) < 0.5e6;
        }
    };

    public boolean compare(float a, float b) {
        return a == b;
    }

    public boolean compare(double a, double b) {
        return a == b;
    }
}
