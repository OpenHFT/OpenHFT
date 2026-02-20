/*
 * Test input for assertion message candidate checks.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputAssertionMessageCandidates {
    void assertionMessages() {
        assertTrue(false, "should emit one candidate");
        assertTrue(false, "result should not be empty");
        assertTrue(false, "i: {}");
        assertTrue(false, "offer i={}");
        assertTrue(false, "don't support index()");
        assertTrue(false, "ignored on hugetlbfs as byte offsets will be different due to page size");
        assertTrue(false, "{} should be read from wire");
        assertTrue(false, "loopstarted called once (priority={})");
        assertTrue(false, "loopfinished called once (priority={})");
        assertTrue(false, "executing test for {} at priority {}");
        assertTrue(false, "throwing handler closed (priority={})");
        assertTrue(false, "empty string should return 0");
        assertTrue(false, "handler closed (priority={})");
        assertTrue(false, "null should return 0");
        assertTrue(false, "don't support two queues yet");
        assertTrue(false, "should skip when multiple comments are present");
        assertTrue(false, "adding {} {} to {}");
        assertTrue(false, "text without placeholders should return 0");
        assertTrue(false, "should emit unhandled warning");
        assertTrue(false, "null should throw npe");
        assertTrue(false, "should use return line");
        assertTrue(false, "version string should match");
        assertTrue(false, "close called once (priority={})");
        assertTrue(false, "no error for {}");
        assertTrue(false, "malformed input around byte");
        assertTrue(false, "native store second slot remains zero");
        assertTrue(false, "{} should start with {}");
        assertTrue(false, "no more messages");
        assertTrue(false, "eventid should be updated in tostring output");
        assertTrue(false, "i={}");
        assertTrue(false, "`transacttime` not set");
        assertTrue(false, "textmethodtester output should match expected");
        assertTrue(false, "iter={}");
        assertTrue(false, "fix guidance should match log guidance");
    }
}
