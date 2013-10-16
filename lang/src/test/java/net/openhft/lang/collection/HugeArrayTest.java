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

package net.openhft.lang.collection;

import net.openhft.lang.model.JavaBeanInterface;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
 * Date: 08/10/13
 * Time: 08:30
 */
public class HugeArrayTest {
    @Test
    public void testHugeArray() {
        // runs with a maximum heap size of 32 MB.
        int length = 10 * 1000 * 1000;
        HugeArray<JavaBeanInterface> array = HugeCollections.newArray(JavaBeanInterface.class, length);
        long start = System.nanoTime();
        for (int i = 0; i < array.length(); i++) {
            JavaBeanInterface jbi = array.get(i);
            jbi.setByte((byte) i);
            jbi.setChar((char) i);
            jbi.setShort((short) i);
            jbi.setInt(i);
            jbi.setFloat(i);
            jbi.setLong(i);
            jbi.setDouble(i);
            jbi.setFlag((i & 3) == 0);
            jbi.setString("hello");
            array.recycle(jbi);
        }
        for (int i = 0; i < array.length(); i++) {
            JavaBeanInterface jbi = array.get(i);
            assertEquals((byte) i, jbi.getByte());
            assertEquals((char) i, jbi.getChar());
            assertEquals((short) i, jbi.getShort());
            assertEquals(i, jbi.getInt());
            assertEquals(i, jbi.getFloat(), 0);
            assertEquals(i, jbi.getLong());
            assertEquals(i, jbi.getDouble(), 0.0);
            assertEquals((i & 3) == 0, jbi.getFlag());
            assertEquals("hello", jbi.getString());
            array.recycle(jbi);
        }
        long time = System.nanoTime() - start;
        double avg = time / 2.0 / length;
        System.out.printf("Average time to access a JavaBeanInterface was %.1f ns%n", avg);
    }
}
