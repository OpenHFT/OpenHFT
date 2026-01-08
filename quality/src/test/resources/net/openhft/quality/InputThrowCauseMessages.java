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

    public void throwWithIndex(long index, long headerNumber) {
        throw new IllegalIndexException(index, headerNumber);
    }

    public void throwWithNamedTailer(String id) {
        throw new NamedTailerNotAvailableException(id,
                NamedTailerNotAvailableException.Reason.NOT_AVAILABLE_ON_SINK);
    }

    public void throwWithMessageFromCause(RuntimeException cause) {
        throw new RuntimeException(cause.getMessage(), cause);
    }

    public void throwWithCauseAndComment(RuntimeException cause) {
        throw new IllegalStateException(cause /* reason */);
    }
}
