package net.openhft.bytes;

/**
 * This allows random access to the underling bytes.  This instance can be used across threads as it is stateless.
 * The thread safety of the underlying data depends on how the methods are used.
 */
public interface RandomDataInput {
}
