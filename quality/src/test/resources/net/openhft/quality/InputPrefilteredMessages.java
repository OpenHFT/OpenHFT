/*
 * Test input for MeaningfulMessageCheck message prefilter handling.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputPrefilteredMessages {

    public void prefilteredMessages() {
        assertTrue(false, "{\"status\":\"bad\"}");
        assertTrue(false, "[{\"status\":\"bad\"}]");
        assertTrue(false, "[[1]]");
        assertTrue(false, "com.example.SomeClass");
        assertTrue(false, "[1,2]");
        assertTrue(false, "\\n");
        assertTrue(false, "!field=value");
        assertTrue(false, "first\nsecond\nthird");
        assertTrue(false, "key=value\nnext=two");
        assertTrue(false, "AlphaBetaGammaDeltaEpsilon");
        assertTrue(false, "alpha beta gamma");
    }
}
