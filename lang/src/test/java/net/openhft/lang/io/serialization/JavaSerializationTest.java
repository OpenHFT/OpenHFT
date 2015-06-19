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

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Rob Austin.
 */
public class JavaSerializationTest {

    @Test
    @Ignore
    public void NullPointerException() {
        final Bytes bytes = ByteBufferBytes.wrap(ByteBuffer.allocate((int) 1024).order
                (ByteOrder.nativeOrder()));

        NullPointerException expected = new NullPointerException("test");
        bytes.writeObject(expected);

        bytes.position(0);
        NullPointerException actual = (NullPointerException)bytes.readObject();

        Assert.assertEquals(expected, actual);
    }
}
