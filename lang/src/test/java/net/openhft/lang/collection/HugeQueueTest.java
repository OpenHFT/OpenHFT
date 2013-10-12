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
import static org.junit.Assert.assertTrue;

/**
 * User: peter.lawrey
 * Date: 08/10/13
 * Time: 13:47
 */
public class HugeQueueTest {
    @Test
    public void testQueue() {
        // runs with a maximum heap size of 32 MB.
        int tests = 5 * 1000 * 1000;
        int length = 1000;
        HugeQueue<JavaBeanInterface> queue = HugeCollections.newQueue(JavaBeanInterface.class, length);
        long start = System.nanoTime();
        for (int j = 0; j < tests; j += length) {
            for (int i = 0; i < length; i++) {
                JavaBeanInterface jbi = queue.offer();
                jbi.setByte((byte) i);
                jbi.setChar((char) i);
                jbi.setShort((short) i);
                jbi.setInt(i);
                jbi.setFloat(i);
                jbi.setLong(i);
                jbi.setDouble(i);
                jbi.setFlag((i & 3) == 0);
                queue.recycle(jbi);
            }
            assertTrue(queue.isFull());
            for (int i = 0; i < length; i++) {
                JavaBeanInterface jbi = queue.take();
                assertEquals((byte) i, jbi.getByte());
                assertEquals((char) i, jbi.getChar());
                assertEquals((short) i, jbi.getShort());
                assertEquals(i, jbi.getInt());
                assertEquals(i, jbi.getFloat(), 0);
                assertEquals(i, jbi.getLong());
                assertEquals(i, jbi.getDouble(), 0.0);
                assertEquals((i & 3) == 0, jbi.getFlag());
                queue.recycle(jbi);
            }
            assertTrue(queue.isEmpty());
        }
        long time = System.nanoTime() - start;
        double avg = time / 2.0 / tests;
        System.out.printf("Average time to access a JavaBeanInterface was %.1f ns%n", avg);

    }
}
