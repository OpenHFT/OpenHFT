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

package net.openhft.lang.io.serialization;

/**
 * A BytesMarshaller with a byte code for the class.
 */
public interface CompactBytesMarshaller<E> extends BytesMarshaller<E> {
    byte BYTE_BUFFER_CODE = 'B' & 31;
    byte CLASS_CODE = 'C' & 31;
    byte INT_CODE = 'I' & 31;
    byte LONG_CODE = 'L' & 31;
    byte DOUBLE_CODE = 'D' & 31;
    byte DATE_CODE = 'T' & 31;
    byte STRING_CODE = 'S' & 31;
    byte STRINGZ_MAP_CODE = 'Y' & 31; // compressed string.
    byte STRINGZ_CODE = 'Z' & 31; // compressed string.
    byte LIST_CODE = '[';
    byte SET_CODE = '[' & 31;
    byte MAP_CODE = '{';

    /**
     * @return the code for this marshaller
     */
    byte code();
}
