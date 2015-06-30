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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.assertEquals;

public class JDKZObjectSerializerTest {

    @Test
    public void testReadSerializable() throws IOException, ClassNotFoundException {
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(bytes.outputStream());
            oos.writeObject("hello");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.release();
        }
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(bytes.outputStream()));
            oos.writeObject("hello world");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello world", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.close();
        }
    }
}