package net.openhft.lang;

import org.junit.Test;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 10:04
 */
public class JvmTest {
    @Test
    public void testIs64Bit() {
        boolean is64 = Jvm.is64Bit();
    }
}
