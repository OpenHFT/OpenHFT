package net.openhft.lang.io;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 09:54
 */
public class IOToolsTest {
    @Test
    public void testClose() {
        final int[] count = {0};
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                count[0]++;
            }
        };
        IOTools.close(c);
        assertEquals(1, count[0]);
        IOTools.close(Arrays.asList(c, c, c));
        assertEquals(4, count[0]);
    }
}
