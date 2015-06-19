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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.util.Date;

/**
 * @author peter.lawrey
 */
public class DateMarshaller implements CompactBytesMarshaller<Date> {
    private final int size1;
    private static final StringBuilderPool sbp = new StringBuilderPool();
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
        StringBuilder sb = sbp.acquireStringBuilder();
        bytes.readUTFΔ(sb);
        long time = parseLong(sb);
        return lookupDate(time);
    }

    @Nullable
    @Override
    public Date read(Bytes bytes, @Nullable Date date) {
        if (date == null)
            return read(bytes);
        StringBuilder sb = sbp.acquireStringBuilder();
        bytes.readUTFΔ(sb);
        long time = parseLong(sb);
        date.setTime(time);
        return date;
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
        return DATE_CODE; // Control T.
    }
}
