/*
 * Test input for throw rethrow patterns and comment suppression.
 * Exercises: ThrowMessageExtractor.isThrowableRethrow() — rethrow, factory, null-constructor paths,
 *            context().hasAdjacentReasonComment() and hasInlineReasonComment().
 */
package net.openhft.quality;

public class InputThrowRethrowPatterns {

    public void rethrowVariable(Exception cause) {
        // propagating original exception
        throw (RuntimeException) cause;
    }

    public void rethrowViaMethod(Exception cause) {
        throw sneakyThrow(cause);
    }

    public void rethrowViaPropagateMethod(Exception cause) {
        throw propagateException(cause);
    }

    public void rethrowViaWrap(Exception cause) {
        throw wrap(cause);
    }

    public void factoryMethodThrow() {
        // deliberately using factory pattern
        throw createError("buffer overflow detected in segment");
    }

    public void nullConstructorThrow() {
        throw new RuntimeException();
    }

    // rethrow with adjacent comment
    public void rethrowWithAdjacentComment(RuntimeException cause) {
        // maintaining original stack trace for diagnostics
        throw cause;
    }

    public void rethrowWithInlineComment(RuntimeException cause) {
        throw cause; // preserving cause chain
    }

    public void factoryWithInlineComment() {
        throw createError("msg"); // factory delegates message
    }

    public void nullConstructorWithComment() {
        // sentinel exception signals end of stream
        throw new IllegalStateException();
    }

    private static RuntimeException sneakyThrow(Exception cause) {
        return new RuntimeException(cause);
    }

    private static RuntimeException propagateException(Exception cause) {
        return new RuntimeException(cause);
    }

    private static RuntimeException wrap(Exception cause) {
        return new RuntimeException(cause);
    }

    private static RuntimeException createError(String message) {
        return new RuntimeException(message);
    }
}
