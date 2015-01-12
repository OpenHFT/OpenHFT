package net.openhft.lang.data.attic;

import net.openhft.lang.data.WireKey;

import java.io.StreamCorruptedException;

/**
 * marshals a different object.
 *
 * Created by peter on 1/10/15.
 */
public interface Marshaller<T, WK extends WireKey> {
    /**
     * Write data to the wire
     *
     * @param wire to write to.
     */
    public void writeMarshallable(T t, Wire wire);

    /**
     * Straight line ordered decoding.
     *
     * @param wire to read from in an ordered manner.
     *             @throws java.io.StreamCorruptedException the stream wasn't ordered or formatted as expected.
     */
    public void readMarshallable(T t, Wire wire) throws StreamCorruptedException;

    public void onMarshallableReset(T t);

    public void onMarshallableKeyValue(T t, WK wireKey, WireValue value);

    public void onMarshallableComplete(T t);
}
