package net.openhft.chronicle.bytes;

/**
 * Created by peter on 16/01/15.
 */
public class EscapingStopCharTester implements StopCharTester {
    private final StopCharTester sct;
    private boolean escaped = false;

    EscapingStopCharTester(StopCharTester sct) {
        this.sct = sct;
    }

    public static StopCharTester escaping(StopCharTester sct) {
        return new EscapingStopCharTester(sct);
    }

    @Override
    public boolean isStopChar(int ch) throws IllegalStateException {
        if (escaped) {
            escaped = false;
            return false;
        }
        if (ch == '\\') {
            escaped = true;
            return false;
        }
        return sct.isStopChar(ch);
    }

}
