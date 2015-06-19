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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

/**
 * @author peter.lawrey
 */
public interface ByteStringAppender extends Appendable, BytesCommon {
    @NotNull
    ByteStringAppender append(@NotNull CharSequence s);

    @NotNull
    ByteStringAppender append(@NotNull CharSequence s, int start, int end);

    /**
     * Writes "true" or "false".
     *
     * @param b to write.
     * @return this.
     */
    @NotNull
    ByteStringAppender append(boolean b);

    @NotNull
    ByteStringAppender append(char c);

    @NotNull
    ByteStringAppender append(@Nullable Enum value);

    @NotNull
    ByteStringAppender append(int i);

    @NotNull
    ByteStringAppender append(long l);

    @NotNull
    ByteStringAppender append(long l, int base);

    @NotNull
    ByteStringAppender appendTimeMillis(long timeInMS);

    @NotNull
    ByteStringAppender appendDateMillis(long timeInMS);

    @NotNull
    ByteStringAppender appendDateTimeMillis(long timeInMS);

//    ByteStringAppender append(float f);

//    ByteStringAppender append(float f, int precision);

    @NotNull
    ByteStringAppender append(double d);

    @NotNull
    ByteStringAppender append(double d, int precision);

    @NotNull
    ByteStringAppender append(@NotNull MutableDecimal md);

    @NotNull
    <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator);
}
