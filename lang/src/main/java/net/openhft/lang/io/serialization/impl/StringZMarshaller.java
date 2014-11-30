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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by peter on 24/10/14.
 */
public enum StringZMarshaller implements CompactBytesMarshaller<String> {
    INSTANCE;
    private static final int NULL_LENGTH = -1;

    @Override
    public byte code() {
        return STRINGZ_CODE;
    }

    @Override
    public void write(Bytes bytes, String s) {
        if (s == null) {
            bytes.writeStopBit(NULL_LENGTH);
            return;
        }
        bytes.writeStopBit(s.length());
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(bytes.outputStream()));
        try {
            dos.writeUTF(s);
            dos.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.writeUnsignedInt(position, bytes.position() - position - 4);
    }

    @Override
    public String read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public String read(Bytes bytes, @Nullable String ignored) {
        long size = bytes.readStopBit();
        if (size == NULL_LENGTH)
            return null;
        if (size < 0 || size > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + size);

        // has to be fixed lenth field, not stop bit.
        long length = bytes.readUnsignedInt();
        if (length < 0 || length > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + length);
        long position = bytes.position();
        long end = position + length;

        long limit = bytes.limit();
        bytes.limit(end);

        DataInputStream dis = new DataInputStream(new InflaterInputStream(bytes.inputStream()));
        String s;
        try {
            s = dis.readUTF();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.position(end);
        bytes.limit(limit);
        return s;
    }
}
