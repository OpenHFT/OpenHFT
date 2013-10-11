/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
