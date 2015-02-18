package net.openhft.core;

public interface Memory {
    void storeFence();

    void loadFence();
}
