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

package net.openhft.lang.io;

import java.util.List;

/**
 * @author peter.lawrey
 */
public interface ByteStringAppender extends Appendable, BytesCommon {
    ByteStringAppender append(CharSequence s);

    ByteStringAppender append(CharSequence s, int start, int end);

    ByteStringAppender append(byte[] str);

    ByteStringAppender append(byte[] str, int offset, int len);

    ByteStringAppender append(boolean b);

    ByteStringAppender append(char c);

    ByteStringAppender append(Enum value);

    ByteStringAppender append(int i);

    ByteStringAppender append(long l);

    ByteStringAppender appendTimeMillis(long timeInMS);

    ByteStringAppender appendDateMillis(long timeInMS);

    ByteStringAppender appendDateTimeMillis(long timeInMS);

//    ByteStringAppender append(float f);

//    ByteStringAppender append(float f, int precision);

    ByteStringAppender append(double d);

    ByteStringAppender append(double d, int precision);

    ByteStringAppender append(MutableDecimal md);

    <E> ByteStringAppender append(E object);

    <E> ByteStringAppender append(Iterable<E> list, CharSequence seperator);

    <E> ByteStringAppender append(List<E> list, CharSequence seperator);
}
