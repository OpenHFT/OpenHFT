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
