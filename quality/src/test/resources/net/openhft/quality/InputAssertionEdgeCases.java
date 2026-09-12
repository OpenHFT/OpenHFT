/*
 * Test input for MeaningfulMessageCheck assertion edge cases.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputAssertionEdgeCases {

    public void comparisonWithMethodCall() {
        assertTrue(count() > limit, "cache entry exceeds minimum threshold");
    }

    public void comparisonWithDot() {
        Stats stats = new Stats();
        assertTrue(stats.total >= limit, "cache entry total meets minimum threshold");
    }

    public void stringSearchWithLiteral() {
        assertTrue("token".startsWith("a"), "cache entry contains required prefix");
    }

    public void stringSearchWithMethodCall() {
        String text = "alpha";
        assertTrue(text.contains(token()), "cache entry contains required token");
    }

    public void methodReferenceMessage() {
        assertTrue(ready(), this::readyMessage);
    }

    public void qualifiedClassLiteral() {
        org.junit.jupiter.api.Assertions.assertThrows(java.lang.IllegalStateException.class, () -> {
        });
    }

    private boolean ready() {
        return true;
    }

    private String readyMessage() {
        return "cache entry should be ready for apply";
    }

    private int count() {
        return 1;
    }

    private String token() {
        return "token";
    }

    private static final class Stats {
        private final int total = 1;
    }

    private final int limit = 2;
}
