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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.NativeBytes;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey Date: 22/09/13 Time: 17:06
 */
public class RawCopierTest {
    static void printInts(Object o, int len) {
        for (long i = 0; i < len; i += 4) {
            System.out.print(NativeBytes.UNSAFE.getInt(o, i) + " ");
        }
        System.out.println();
    }

    @Test
    public void testStartEnd() {
        RawCopier<A> aRawCopier = RawCopier.copies(A.class);
        if (aRawCopier.start != 8)
            assertEquals(12, aRawCopier.start);
        assertEquals(aRawCopier.start + 3 * 4, aRawCopier.end);

        RawCopier<B> bRawCopier = RawCopier.copies(B.class);
        if (aRawCopier.start != 8)
            assertEquals(16, bRawCopier.start);
        assertEquals(bRawCopier.start + 4 * 8, bRawCopier.end);
    }

    @Test
    public void testReadWrite() {
        DirectStore ds = new DirectStore(null, 1024);
        DirectBytes db = ds.createSlice();
        RawCopier<A> aRawCopier = RawCopier.copies(A.class);
        A a = new A();
        a.i = 111;
        a.j = -222;
        a.k = 333;
        a.s = "Hello";
        aRawCopier.toBytes(a, db);
        assertEquals(12, db.position());

        assertEquals(111, db.readInt(0));
        assertEquals(-222, db.readInt(4));
        assertEquals(333, db.readInt(8));

        A a2 = new A();
        a2.i = 1;
        a2.j = 2;
        a2.k = 3;
//        printInts(a2, 28);
        db.position(0);
        aRawCopier.fromBytes(db, a2);
//        printInts(a2, 28);
        assertEquals(111, a2.i);
        assertEquals(-222, a2.j);
        assertEquals(333, a2.k);
        assertEquals(null, a2.s);
    }

    static class A {
        int i, j, k;
        String s;
        transient Map map;
    }

    static class B extends Point2D.Double {
        double z, w;
    }
}
