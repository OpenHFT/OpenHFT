/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
 * User: peter.lawrey Date: 08/10/13 Time: 08:30
 */
public class HugeArrayTest {
    static void assertEquals2(long a, long b) {
        if (a != b)
            org.junit.Assert.assertEquals(a, b);
    }

    /*
     * Test proves that you can get more than one object from the array
     * The first object is created at construction time to get the size of an element
     * The second one is created on the fly due to freelist being empty.
     * There was a bug where acquire() was creating a heap object on freelist miss
     */

    @Test
    public void testGetTwoObjects() {
        HugeArray<JavaBeanInterface> array =
                HugeCollections.newArray(JavaBeanInterface.class, 2);
        JavaBeanInterface obj1 = array.get(0);
        JavaBeanInterface obj2 = array.get(1);
/*
Can't call recycle due to recycle using .equals checks on your model.
Two unmodified objects are .equals().
Recycle should use identity to know if it's putting the same object back in the list.

        array.recycle(obj1);
        array.recycle(obj2);
*/
    }

    /*
    With lock: false, average time to access a JavaBeanInterface was 71.9 ns
    With lock: true, average time to access a JavaBeanInterface was 124.7 ns
     */

    @Test
    public void testHugeArray() throws InterruptedException {
        // runs with a maximum heap size of 32 MB.
        int length = 10 * 1000 * 1000;
        HugeArray<JavaBeanInterface> array =
                HugeCollections.newArray(JavaBeanInterface.class, length);
        for (boolean withLock : new boolean[]{false, true}) {
            long start = System.nanoTime();
            for (int i = 0; i < array.length(); i++) {
                JavaBeanInterface jbi = array.get(i);
                if (withLock)
                    jbi.busyLockRecord();
                try {
                    jbi.setByte((byte) i);
                    jbi.setChar((char) i);
                    jbi.setShort((short) i);
                    jbi.setInt(i);
                    jbi.setFloat(i);
                    jbi.setLong(i); // System.nanoTime());
                    jbi.setDouble(i);
                    jbi.setFlag((i & 3) == 0);
                    jbi.setString("hello");
                } finally {
                    if (withLock)
                        jbi.unlockRecord();
                }
                array.recycle(jbi);
            }
            for (int i = 0; i < array.length(); i++) {
                JavaBeanInterface jbi = array.get(i);
                if (withLock)
                    jbi.busyLockRecord();
                try {
                    assertEquals2((byte) i, jbi.getByte());
                    assertEquals2((char) i, jbi.getChar());
                    assertEquals2((short) i, jbi.getShort());
                    assertEquals2(i, jbi.getInt());
                    assertEquals(i, jbi.getFloat(), 0);
//            long time = System.nanoTime() - jbi.getLong();
                    assertEquals2(i, jbi.getLong());
                    assertEquals(i, jbi.getDouble(), 0.0);
                    assertEquals((i & 3) == 0, jbi.getFlag());
                    assertEquals("hello", jbi.getString());
                } finally {
                    if (withLock)
                        jbi.unlockRecord();
                }
                array.recycle(jbi);
            }
            long time = System.nanoTime() - start;
            double avg = time / 2.0 / length;
            System.out.printf("With lock: %s, average time to access a JavaBeanInterface was %.1f ns%n", withLock, avg);
        }
    }
}
