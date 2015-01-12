package net.openhft.lang.data;

import java.io.StreamCorruptedException;

/**
 * Created by peter on 1/10/15.
 */
public interface Marshallable<WK extends WireKey> {
    /**
     * Write data to the wire
     *
     * @param wire to write to.
     */
    public void writeMarshallable(Wire wire);

    /**
     * Straight line ordered decoding.
     *
     * @param wire to read from in an ordered manner.
     *             @throws java.io.StreamCorruptedException the stream wasn't ordered or formatted as expected.
     */
    public void readMarshallable(Wire wire) throws StreamCorruptedException;

    public void onMarshallableReset();

    public void onMarshallableKeyValue(WK wireKey, WireValue value);

    public void onMarshallableComplete();
}
