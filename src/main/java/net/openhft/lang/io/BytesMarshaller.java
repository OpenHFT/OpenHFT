package net.openhft.lang.io;

/**
 * External marshaller for classes.
 *
 * @author peter.lawrey
 * @see BytesMarshallable
 */
public interface BytesMarshaller<E> {
    /**
     * @return super class of classes marshalled/
     */
    public Class<E> classMarshaled();

    /**
     * write the object out as bytes.
     *
     * @param bytes to write to
     * @param e     to write
     */
    public void write(Bytes bytes, E e);

    /**
     * Read bytes and obtain an object
     *
     * @param bytes to read
     * @return the object
     */
    public E read(Bytes bytes);

    /**
     * Write in a form which can be parsed
     *
     * @param bytes to write to
     * @param e     to write
     */
    public void append(Bytes bytes, E e);

    /**
     * Parse a stream up to a known stop character
     *
     * @param bytes  to read
     * @param tester to know when to stop.
     * @return the object.
     */
    public E parse(Bytes bytes, StopCharTester tester);
}
