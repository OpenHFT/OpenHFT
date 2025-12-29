/*
 * Test input for MeaningfulMessageCheck throw cause handling.
 */
package net.openhft.quality;

public class InputThrowCauseMessages {

    public void throwWithCause(RuntimeException cause) {
        throw new IllegalStateException(cause);
    }

    public void throwWithNewCause() {
        throw new IllegalStateException(new RuntimeException("boom"));
    }

    public void throwWithMessageAndCause(RuntimeException cause) {
        throw new IllegalStateException("cache entry should be ready", cause);
    }
}
