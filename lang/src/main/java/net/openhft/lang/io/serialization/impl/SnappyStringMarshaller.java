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
import org.xerial.snappy.SnappyFramedInputStream;
import org.xerial.snappy.SnappyFramedOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Created by peter on 24/10/14.
 */
public enum SnappyStringMarshaller implements CompactBytesMarshaller<CharSequence> {
    INSTANCE;
    private static final Constructor NEW_STRING;
    private static final Field VALUE;

    private static final int NULL_LENGTH = -1;

    static {
        try {
            NEW_STRING = String.class.getDeclaredConstructor(char[].class, boolean.class);
            NEW_STRING.setAccessible(true);
            VALUE = String.class.getDeclaredField("value");
            VALUE.setAccessible(true);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public byte code() {
        return STRINGZ_CODE;
    }

    @Override
    public void write(Bytes bytes, CharSequence s) {
        if (s == null) {
            bytes.writeStopBit(NULL_LENGTH);
            return;
        }
        bytes.writeStopBit(s.length());
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        try {
            DataOutputStream dos = new DataOutputStream(/*new BufferedOutputStream*/(new SnappyFramedOutputStream(bytes.outputStream())/*, 512*/));
            dos.writeInt(s.length());
            char[] chars = (char[]) VALUE.get(s);
            for (int i = 0, len = s.length(); i < len; i++)
                writeStopBit(dos, chars[i]);
            dos.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        bytes.writeUnsignedInt(position, bytes.position() - position - 4);
    }

    private void writeStopBit(DataOutputStream dos, int i) throws IOException {
        if (i < 128) {
            dos.write(i);
        } else if (i < 1 << 14) {
            dos.write((i >>> 7) | 0x80);
            dos.write(i & 0x7F);
        } else {
            dos.write((i >>> 14) | 0x80);
            dos.write((i >>> 7) | 0x80);
            dos.write(i & 0x7F);
        }
    }

    @Override
    public String read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public String read(Bytes bytes, @Nullable CharSequence ignored) {
        long size = bytes.readStopBit();
        if (size == NULL_LENGTH)
            return null;
        if (size < 0 || size > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + size);

        // has to be fixed length field, not stop bit.
        long length = bytes.readUnsignedInt();
        if (length < 0 || length > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + length);
        long position = bytes.position();
        long end = position + length;

        long limit = bytes.limit();
        bytes.limit(end);

        String s;
        try {
            DataInputStream dis = new DataInputStream(/*new BufferedInputStream*/(new SnappyFramedInputStream(bytes.inputStream())/*, 512*/));
            int len = dis.readInt();
            char[] chars = new char[len];
            for (int i = 0; i < len; i++)
                chars[i] = readStopBit(dis);
            s = (String) NEW_STRING.newInstance(chars, true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        bytes.position(end);
        bytes.limit(limit);
        return s;
    }

    private char readStopBit(DataInputStream dis) throws IOException {
        int b0 = dis.read();
        if (b0 < 128)
            return (char) b0;
        int b1 = dis.read();
        if (b1 < 128)
            return (char) (((b0 & 0x7f) << 7) | b1);
        int b2 = dis.read();
        return (char) (((b0 & 0x7f) << 14) | ((b1 & 0x7f) << 7) | b2);
    }
}
