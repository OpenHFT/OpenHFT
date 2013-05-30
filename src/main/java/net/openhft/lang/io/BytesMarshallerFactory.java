package net.openhft.lang.io;

/**
 * @author peter.lawrey
 */
public interface BytesMarshallerFactory {
    <E> BytesMarshaller<E> acquireMarshaller(Class<E> eClass, boolean create);
}
