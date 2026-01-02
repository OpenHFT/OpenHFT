/*
 * Test input for throw new and requireNonNull message checks.
 */
package net.openhft.quality;

import static java.util.Objects.requireNonNull;

public class InputThrowNewMessages {

    public void testThrowNew(String value) {
        if (value == null) {
            throw new IllegalArgumentException("bad input");
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse header value");
        }
    }

    public void testRequireNonNull(Object value) {
        requireNonNull(value, "expected");
    }

    public void testRequireNonNullDuplicates(Object left, Object right) {
        requireNonNull(left, "user id must be present");
        requireNonNull(right, "user id must be present");
    }

    public void testThrowDuplicates() {
        throw new IllegalStateException("configuration snapshot not loaded");
    }

    public void testThrowDuplicatesAgain() {
        throw new IllegalStateException("configuration snapshot not loaded");
    }

    public void testAssertStatement(boolean ok) {
        assert ok : "line 42";
    }

    public void testThrowMissingMessage() {
        throw new IllegalStateException();
    }

    public void testThrowMissingMessageWithComment() {
        throw new IllegalStateException(/* condition not met */);
    }

    public void testHelperDuplicateMessage() {
        throwHelperDuplicate();
    }

    public void testHelperDuplicateMessageAgain() {
        throwHelperDuplicate();
    }

    private static void throwHelperDuplicate() {
        throw new IllegalStateException("duplicate message provided by helper method");
    }
}
