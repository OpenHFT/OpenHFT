/*
 * Test input for low-signal assertion message detection.
 * NOTE: Each message must be unique across the file to avoid DUPLICATE warnings.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

public class InputLowSignalMessages {

    // ============================================
    // MSG_GENERIC - Generic single-word messages
    // These are single words that provide no context
    // ============================================

    public void testGenericMessages() {
        assertEquals(1, 1, "actual");      // line 17 - generic only
        assertEquals(1, 1, "expected");    // line 18 - generic only
        assertEquals(1, 1, "value");       // line 19 - generic only
        assertEquals(1, 1, "result");      // line 20 - generic only
        assertEquals(1, 1, "data");        // line 21 - generic only
        assertEquals(1, 1, "object");      // line 22 - generic only
        assertEquals(1, 1, "condition");   // line 23 - generic only
        assertEquals(1, 1, "message");     // line 24 - generic only
        assertEquals(1, 1, "msg");         // line 25 - generic only
        assertEquals(1, 1, "ok");          // line 26 - generic only
    }

    // ============================================
    // MSG_RESTATES_ASSERTION - Assertion type repetition
    // Using unique first args to avoid DUPLICATE warnings
    // ============================================

    public void testRestatesAssertion() {
        assertEquals(1, 1, "assertEquals");         // line 35 - restates
        assertTrue(true, "assertTrue");             // line 36 - restates
        assertFalse(false, "assertFalse");          // line 37 - restates
        assertNull(null, "assertNull");             // line 38 - restates
        // JUnit 5: assertNotNull(actual, message) - message last
        assertNotNull(new Object(), "assertNotNull");   // line 40 - restates
        assertTrue(true, "should be true");         // line 41 - restates
        assertFalse(false, "should be false");      // line 42 - restates
        assertNull(null, "should be null");         // line 43 - restates
        assertNotNull(new Object(), "should not be null"); // line 44 - restates
        assertEquals(1, 1, "must match");           // line 45 - restates
        assertEquals(1, 1, "should match");         // line 46 - restates
        assertEquals(1, 1, "equals");               // line 47 - restates
        assertNotNull(new Object(), "not null");    // line 48 - restates
        assertNull(null, "is null");                // line 49 - restates
        assertTrue(true, "is true");                // line 50 - restates
        assertFalse(false, "is false");             // line 51 - restates
    }

    // ============================================
    // MSG_INDEX_ONLY - Index-only messages
    // ============================================

    public void testIndexOnlyMessages() {
        assertEquals(1, 1, "0");          // line 56 - index only
        assertEquals(1, 1, "1");          // line 57 - index only
        assertEquals(1, 1, "[0]");        // line 58 - index only
        assertEquals(1, 1, "[5]");        // line 59 - index only
        assertEquals(1, 1, "index 0");    // line 60 - index only
        assertEquals(1, 1, "index 42");   // line 61 - index only
        assertEquals(1, 1, "element 0");  // line 62 - index only
        assertEquals(1, 1, "item 3");     // line 63 - index only
        assertEquals(1, 1, "#0");         // line 64 - index only
        assertEquals(1, 1, "i=5");        // line 65 - index only
    }

    // ============================================
    // MSG_CONTEXTLESS - Contextless comparison messages
    // Using unique messages to avoid DUPLICATE warnings
    // ============================================

    public void testContextlessMessages() {
        assertEquals(1, 1, "comparison");          // line 73 - contextless only
        assertEquals(1, 1, "validation");          // line 74 - contextless only
        assertEquals(1, 1, "equality");            // line 75 - contextless only
        assertEquals(1, 1, "mismatch");            // line 76 - contextless only
        assertEquals(1, 1, "failed");              // line 77 - contextless only
        assertEquals(1, 1, "failure");             // line 78 - contextless only
    }

    // ============================================
    // Combined: GENERIC + CONTEXTLESS
    // ============================================

    public void testGenericAndContextless() {
        assertEquals(1, 1, "test");   // line 86 - generic + contextless
        assertEquals(1, 1, "check");  // line 87 - generic + contextless
        assertEquals(1, 1, "error");  // line 88 - generic + contextless
    }

    // ============================================
    // Combined: RESTATES_ASSERTION + CONTEXTLESS
    // ============================================

    public void testRestatesAndContextless() {
        assertEquals(1, 1, "should be equal"); // line 96 - restates + contextless
        assertEquals(1, 1, "must be equal");   // line 97 - restates + contextless
        assertEquals(1, 1, "should equal");    // line 98 - restates + contextless
        assertEquals(1, 1, "must equal");      // line 99 - restates + contextless
        assertEquals(1, 1, "values should match"); // line 100 - restates + contextless
        assertEquals(1, 1, "value should match");  // line 101 - restates + contextless
    }

    // ============================================
    // Valid messages - should NOT produce warnings
    // ============================================

    public void testValidMessages() {
        // Good: explains what's being tested
        assertEquals(5, getCount(), "user count should be 5 after filtering");

        // Good: provides business context
        assertTrue(isValid(), "order should be valid after setting required fields");

        // Good: specific about the data
        assertEquals("AAPL", getSymbol(), "first symbol should be AAPL");

        // Good: describes the operation
        assertEquals(original, copy, "object should survive round-trip serialization");

        // Good: index with context
        assertEquals(expected, array[0], "first element should be header sentinel");

        // Good: specific failure description
        assertNotNull(result, "query result should not be null for valid input");
    }

    private int getCount() { return 5; }
    private boolean isValid() { return true; }
    private String getSymbol() { return "AAPL"; }
    private Object original = new Object();
    private Object copy = original;
    private Object expected = null;
    private Object[] array = new Object[1];
    private Object result = new Object();
}
