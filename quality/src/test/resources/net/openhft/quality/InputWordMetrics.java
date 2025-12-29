/*
 * Test input for word count and word length checks.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

public class InputWordMetrics {

    // ============================================
    // MSG_TOO_SHORT - Messages with too few words
    // ============================================

    public void testTooShortMessages() {
        // 1 word - should warn (line 16)
        assertEquals("short", 1, 1);
        // 2 words - should warn (line 18)
        assertEquals("too short", 2, 2);
    }

    // ============================================
    // MSG_TOO_FEW_MEANINGFUL - Not enough meaningful words
    // ============================================

    public void testTooFewMeaningfulWords() {
        // 4 words, but only 1 meaningful word - should warn (line 26)
        assertEquals("expected result ok value", 1, 1);
    }

    // ============================================
    // MSG_TOO_LONG - Messages with too many words
    // ============================================

    public void testTooLongMessage() {
        // 43 words - should warn (line 35)
        assertEquals("one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty twentyone twentytwo twentythree twentyfour twentyfive twentysix twentyseven twentyeight twentynine thirty thirtyone thirtytwo thirtythree thirtyfour thirtyfive thirtysix thirtyseven thirtyeight thirtynine forty fortyone fortytwo fortythree", 1, 1);
    }

    // ============================================
    // MSG_LONG_WORD - Words exceeding 42 characters
    // ============================================

    public void testLongWord() {
        // Word "SomeExtremelyVeryLongClassNameThatExceedsLimitHere" (50 chars) - should warn (line 44)
        assertEquals("error in SomeExtremelyVeryLongClassNameThatExceedsLimitHere here", 1, 1);
        // Word "AnotherIncrediblyLongWordThatExceedsFortyTwoCharacters" (54 chars) - should warn (line 46)
        assertEquals("problem with AnotherIncrediblyLongWordThatExceedsFortyTwoCharacters detected", 1, 1);
    }

    // ============================================
    // Valid messages - should NOT produce warnings
    // ============================================

    public void testValidMessages() {
        // Exactly 4 words - OK
        assertEquals("four words right here", 1, 1);
        // Exactly 42 words - OK
        assertEquals("one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty twentyone twentytwo twentythree twentyfour twentyfive twentysix twentyseven twentyeight twentynine thirty thirtyone thirtytwo thirtythree thirtyfour thirtyfive thirtysix thirtyseven thirtyeight thirtynine forty fortyone fortytwo", 1, 1);
        // Word exactly 16 chars - OK
        assertEquals("sixteencharword is fine here", 1, 1);
    }
}
