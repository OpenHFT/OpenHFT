package net.openhft.core;

public interface Memory {
    void storeFence();

    void loadFence();

    byte readByte(Object object, long offset);

    void writeByte(Object object, long offset, byte b);
}
