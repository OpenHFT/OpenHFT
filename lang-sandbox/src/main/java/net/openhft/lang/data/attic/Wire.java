package net.openhft.lang.data.attic;

import net.openhft.lang.data.WireKey;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.Nullable;

/**
 * Created by peter on 1/10/15.
 */
public interface Wire {
    Bytes getBytes();

    void writeBoolean(WireKey key, boolean value);
    boolean readBoolean(WireKey key);

    void writeByte(WireKey key, int value);
    byte readByte(WireKey key);

    void writeUnsignedByte(WireKey key, int value);
    short readUnsignedByte(WireKey key);

    void writeShort(WireKey key, int value);
    short readShort(WireKey key);

    void writeUnsignedShort(WireKey key, int value);
    int readUnsignedShort(WireKey key);

    void writeInt(WireKey key, int value);
    int readInt(WireKey key);

    void writeUnsignedInt(WireKey key, long value);
    long readUnsignedInt(WireKey key);

    void writeLong(WireKey key, long value);
    long readLong(WireKey key);

    void writeFloat(WireKey key, float value);
    float readFloat(WireKey key);

    void writeDouble(WireKey key, double value);
    double readDouble(WireKey key);

    void writeText(WireKey key, CharSequence value);
    String readText(WireKey key);
    boolean readText(WireKey key, StringBuilder sb);

    void writeEnum(WireKey key, Enum value);
    Enum readEnum(WireKey key);
    Enum readEnum(WireKey key, @Nullable StringBuilder value);


}
