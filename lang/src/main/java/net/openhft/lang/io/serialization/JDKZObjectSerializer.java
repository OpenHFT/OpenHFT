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

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public enum JDKZObjectSerializer implements ObjectSerializer {
    INSTANCE;

    @Override
    public void writeSerializable(Bytes bytes, Object object, Class expectedClass) throws IOException {
        // reset the finished flag and append
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        OutputStream out = bytes.outputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(out));
        oos.writeObject(object);
        oos.close();
        long length = bytes.position() - position - 4;
        bytes.writeUnsignedInt(position, length);
    }

    @Override
    public <T> T readSerializable(@NotNull Bytes bytes, Class<T> expectedClass, T object) throws IOException, ClassNotFoundException {
        long length = bytes.readUnsignedInt();
        if (length < 8 || length > Integer.MAX_VALUE)
            throw new StreamCorruptedException("length = " + Long.toHexString(length));
        long end = bytes.position() + length;
        long lim = bytes.limit();
        bytes.limit(end);
        int magic = bytes.readUnsignedShort(bytes.position());
        InputStream in = bytes.inputStream();
        switch (magic) {
            case 0xEDAC:
                break;

            case 0x9c78:
                in = new InflaterInputStream(in);
                break;
            default:
                throw new StreamCorruptedException("Unknown magic number " + Integer.toHexString(magic));
        }
        T t = (T) new ObjectInputStream(in).readObject();
        bytes.limit(lim);
        if (end != bytes.position()) {
            System.out.println("diff: " + (end - bytes.position()));
            bytes.position(end);
        }
        return t;
    }
}
