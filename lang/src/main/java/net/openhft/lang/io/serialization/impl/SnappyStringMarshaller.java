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

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

/**
 * Created by peter on 24/10/14.
 */
public enum SnappyStringMarshaller implements CompactBytesMarshaller<CharSequence> {
    INSTANCE;
    private static final StringFactory STRING_FACTORY = getStringFactory();

    private static final int NULL_LENGTH = -1;

    private static StringFactory getStringFactory() {
        try {
            return new StringFactory17();
        } catch (Exception e) {
            // do nothing
        }

        try {
            return new StringFactory16();
        } catch (Exception e) {
            // no more alternatives
            throw new AssertionError(e);
        }
    }

    private static final ThreadLocal<ThreadLocals> THREAD_LOCALS = new ThreadLocal<ThreadLocals>();

    static class ThreadLocals {
        ByteBuffer decompressedByteBuffer = ByteBuffer.allocateDirect(32 * 1024);
        Bytes decompressedBytes = new ByteBufferBytes(decompressedByteBuffer);
        ByteBuffer compressedByteBuffer = ByteBuffer.allocateDirect(0);

        public void clear() {
            decompressedByteBuffer.clear();
            decompressedBytes.clear();
            compressedByteBuffer.clear();
        }
    }

    @Override
    public byte code() {
        return STRINGZ_CODE;
    }

    public ThreadLocals acquireThreadLocals() {
        ThreadLocals threadLocals = THREAD_LOCALS.get();
        if (threadLocals == null)
            THREAD_LOCALS.set(threadLocals = new ThreadLocals());
        threadLocals.clear();
        return threadLocals;
    }

    @Override
    public void write(Bytes bytes, CharSequence s) {
        if (s == null) {
            bytes.writeStopBit(NULL_LENGTH);
            return;
        } else if (s.length() == 0) {
            bytes.writeStopBit(0);
            return;
        }
        // write the total length.
        int length = s.length();
        bytes.writeStopBit(length);

        ThreadLocals threadLocals = acquireThreadLocals();
        // stream the portions of the string.
        Bytes db = threadLocals.decompressedBytes;
        ByteBuffer dbb = threadLocals.decompressedByteBuffer;
        ByteBuffer cbb = bytes.sliceAsByteBuffer(threadLocals.compressedByteBuffer);

        int position = 0;
        while (position < length) {
            // 3 is the longest encoding.
            while (position < length && db.remaining() >= 3)
                db.writeStopBit(s.charAt(position++));
            dbb.position(0);
            dbb.limit((int) db.position());
            // portion copied now compress it.
            int portionLengthPos = cbb.position();
            cbb.putShort((short) 0);
            int compressedLength;
            try {
                Snappy.compress(dbb, cbb);
                compressedLength = cbb.remaining();
                if (compressedLength >= 1 << 16)
                    throw new AssertionError();
                // unflip.
                cbb.position(cbb.limit());
                cbb.limit(cbb.capacity());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            cbb.putShort(portionLengthPos, (short) compressedLength);
            db.clear();
        }
        // the end.
        cbb.putShort((short) 0);
        bytes.position(bytes.position() + cbb.position());
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
        if (size == 0)
            return "";
        ThreadLocals threadLocals = acquireThreadLocals();
        // stream the portions of the string.
        Bytes db = threadLocals.decompressedBytes;
        ByteBuffer dbb = threadLocals.decompressedByteBuffer;
        ByteBuffer cbb = bytes.sliceAsByteBuffer(threadLocals.compressedByteBuffer);

        char[] chars = new char[(int) size];
        int pos = 0;
        for (int chunkLen; (chunkLen = cbb.getShort() & 0xFFFF) > 0; ) {
            cbb.limit(cbb.position() + chunkLen);
            dbb.clear();
            try {
                Snappy.uncompress(cbb, dbb);
                cbb.position(cbb.limit());
                cbb.limit(cbb.capacity());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            db.position(0);
            db.limit(dbb.limit());
            while (db.remaining() > 0)
                chars[pos++] = (char) db.readStopBit();
        }
        bytes.position(bytes.position() + cbb.position());
        try {
            return STRING_FACTORY.fromChars(chars);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static abstract class StringFactory {
        abstract String fromChars(char[] chars) throws Exception;
    }

    private static final class StringFactory16 extends StringFactory {
        private final Constructor<String> constructor;

        private StringFactory16() throws Exception {
            constructor = String.class.getDeclaredConstructor(int.class,
                    int.class, char[].class);
            constructor.setAccessible(true);
        }

        @Override
        String fromChars(char[] chars) throws Exception {
            return constructor.newInstance(0, chars.length, chars);
        }
    }

    private static final class StringFactory17 extends StringFactory {
        private final Constructor<String> constructor;

        private StringFactory17() throws Exception {
            constructor = String.class.getDeclaredConstructor(char[].class, boolean.class);
            constructor.setAccessible(true);
        }

        @Override
        String fromChars(char[] chars) throws Exception {
            return constructor.newInstance(chars, true);
        }
    }
}
