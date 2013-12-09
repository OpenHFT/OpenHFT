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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * @author peter.lawrey
 */
public class DateMarshaller implements CompactBytesMarshaller<Date> {
    final int size1;
    private final StringBuilder sb = new StringBuilder();
    @Nullable
    private Date[] interner = null;

    public DateMarshaller(int size) {
        int size2 = 128;
        while (size2 < size && size2 < (1 << 20)) size2 <<= 1;
        this.size1 = size2 - 1;
    }

    private static long parseLong(@NotNull CharSequence sb) {
        long num = 0;
        boolean negative = false;
        for (int i = 0; i < sb.length(); i++) {
            char b = sb.charAt(i);
//            if (b >= '0' && b <= '9')
            if ((b - ('0' + Integer.MIN_VALUE)) <= 9 + Integer.MIN_VALUE)
                num = num * 10 + b - '0';
            else if (b == '-')
                negative = true;
            else
                break;
        }
        return negative ? -num : num;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull Date date) {
        long pos = bytes.position();
        bytes.writeUnsignedByte(0);
        bytes.append(date.getTime());
        bytes.writeUnsignedByte(pos, (int) (bytes.position() - 1 - pos));
    }

    @Nullable
    @Override
    public Date read(@NotNull Bytes bytes) {
        bytes.readUTFΔ(sb);
        long time = parseLong(sb);
        return lookupDate(time);
    }

    @Nullable
    private Date lookupDate(long time) {
        int idx = hashFor(time);
        if (interner == null)
            interner = new Date[size1 + 1];
        Date date = interner[idx];
        if (date != null && date.getTime() == time)
            return date;
        return interner[idx] = new Date(time);
    }

    private int hashFor(long time) {
        long h = time;
        h ^= (h >>> 41) ^ (h >>> 20);
        h ^= (h >>> 14) ^ (h >>> 7);
        return (int) (h & size1);
    }

    @Override
    public byte code() {
        return 'T' & 31; // Control T.
    }
}
