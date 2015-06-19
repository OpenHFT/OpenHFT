/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.io;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
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
