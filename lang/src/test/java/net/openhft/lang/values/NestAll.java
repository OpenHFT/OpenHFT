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

package net.openhft.lang.values;

/**
 * User: peter.lawrey
 * Date: 11/10/13
 * Time: 08:59
 */
public interface NestAll {
    BooleanValue getBoolean();

    ByteValue getByte();

    CharValue getChar();

    DoubleValue getDouble();

    FloatValue getFloat();

    Int24Value getInt24();

    Int48Value getInt48();

    IntValue getInt();

    LongValue getLong();

    ShortValue getShort();

    StringValue getString();

    UnsignedByteValue getUB();

    UnsignedIntValue getUI();

    UnsignedShortValue getUS();
}
