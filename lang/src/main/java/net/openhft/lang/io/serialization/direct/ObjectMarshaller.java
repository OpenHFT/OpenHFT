package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.io.Bytes;

import static net.openhft.lang.io.NativeBytes.UNSAFE;
import static net.openhft.lang.io.serialization.direct.DirectSerializationMetadata.SerializationMetadata;

public final class ObjectMarshaller<T> {
    private final SerializationMetadata metadata;

    public ObjectMarshaller(SerializationMetadata metadata) {
        this.metadata = metadata;
    }

    public void write(Bytes bytes, T tObject) {
        long i = metadata.start;
        long end = metadata.start + metadata.length;

        while (i < end - 7) {
            bytes.writeLong(UNSAFE.getLong(tObject, i));
            i += 8;
        }

        while (i < end) {
            bytes.writeByte(UNSAFE.getByte(tObject, i));
            ++i;
        }
    }

    public T read(Bytes bytes, T tObject) {
        long i = metadata.start;
        long end = metadata.start + metadata.length;

        while (i < end - 7) {
            UNSAFE.putLong(tObject, i, bytes.readLong());
            i += 8;
        }

        while (i < end) {
            UNSAFE.putByte(tObject, i, bytes.readByte());
            ++i;
        }

        return tObject;
    }

    public long length() {
        return metadata.length;
    }
}