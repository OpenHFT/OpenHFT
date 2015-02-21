package net.openhft.chronicle.core;

import org.junit.Test;

public class OSTest {
    @Test
    public void testIs64Bit() {
        System.out.println("is64 = " + OS.is64Bit());
    }

    @Test
    public void testGetProcessId() {
        System.out.println("pid = " + OS.getProcessId());
    }
}