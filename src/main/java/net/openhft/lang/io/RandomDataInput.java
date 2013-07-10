/*
 * Copyright ${YEAR} Peter Lawrey
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

package net.openhft.lang.io;

import java.io.ObjectInput;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.RandomAccess;

/**
 * @author peter.lawrey
 */
public interface RandomDataInput extends ObjectInput, RandomAccess, BytesCommon {
    @Override
    void readFully(byte[] b);

    @Override
    void readFully(byte[] b, int off, int len);

    @Override
    int skipBytes(int n);

    @Override
    boolean readBoolean();

    boolean readBoolean(long offset);

    @Override
    byte readByte();

    byte readByte(long offset);

    @Override
    int readUnsignedByte();

    int readUnsignedByte(long offset);

    @Override
    short readShort();

    short readShort(long offset);

    @Override
    int readUnsignedShort();

    int readUnsignedShort(long offset);

    short readCompactShort();

    int readCompactUnsignedShort();

    @Override
    char readChar();

    char readChar(long offset);

    /**
     * @return a 24-bit integer value.
     */
    int readInt24();

    /**
     * @param offset of start.
     * @return a 24-bit integer value.
     */
    int readInt24(long offset);

    @Override
    int readInt();

    int readInt(long offset);

    int readVolatileInt();

    int readVolatileInt(long offset);

    long readUnsignedInt();

    long readUnsignedInt(long offset);

    int readCompactInt();

    long readCompactUnsignedInt();

    @Override
    long readLong();

    long readLong(long offset);

    long readVolatileLong();

    long readVolatileLong(long offset);

    /**
     * @return read a 48 bit long value.
     */
    long readInt48();

    /**
     * @param offset
     * @return read a 48 bit long value.
     */
    long readInt48(long offset);

    long readCompactLong();

    /**
     * @return a stop bit encoded number as a long.
     */
    long readStopBit();

    @Override
    float readFloat();

    float readFloat(long offset);

    @Override
    double readDouble();

    double readDouble(long offset);

    double readCompactDouble();

    @Override
    String readLine();

    void readByteString(StringBuilder sb);

    void readChars(StringBuilder sb);

    @Override
    String readUTF();

    /**
     * Use readUTF(StringBuilder) or appendUTF
     *
     * @deprecated to be removed in version 1.8
     */
    @Deprecated
    boolean readUTF(Appendable appendable);

    boolean readUTF(StringBuilder stringBuilder);

    boolean appendUTF(Appendable appendable);

    String readUTF(int offset);

    void read(ByteBuffer bb);

    <E> E readEnum(Class<E> eClass);

    <E> void readList(Collection<E> list);

    <K, V> Map<K, V> readMap(Class<K> kClass, Class<V> vClass);

    // ObjectInput
    @Override
    Object readObject() throws IllegalStateException;

    @Override
    int read();

    @Override
    int read(byte[] b);

    @Override
    int read(byte[] b, int off, int len);

    @Override
    long skip(long n);

    /**
     * @return remaining() or Integer.MAX_VALUE if larger.
     */
    @Override
    int available();

    @Override
    void close();
}
