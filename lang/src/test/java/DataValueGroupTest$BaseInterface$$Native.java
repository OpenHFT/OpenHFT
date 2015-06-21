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

package net.openhft.lang;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.Copyable;

import static net.openhft.lang.Compare.calcLongHashCode;
import static net.openhft.lang.Compare.isEqual;

public class DataValueGroupTest$BaseInterface$$Native implements GroupTest.BaseInterface, BytesMarshallable, Byteable, Copyable<GroupTest.BaseInterface> {
    private static final int INT = 0;
    private static final int STR = 4;

    private Bytes _bytes;
    private long _offset;

    public void setInt(int $) {
        _bytes.writeInt(_offset + INT, $);
    }

    public int getInt() {
        return _bytes.readInt(_offset + INT);
    }

    public void setStr(java.lang.String $) {
        _bytes.writeUTFΔ(_offset + STR, 15, $);
    }

    public java.lang.String getStr() {
        return _bytes.readUTFΔ(_offset + STR);
    }

    @Override
    public void copyFrom(GroupTest.BaseInterface from) {
        setInt(from.getInt());
        setStr(from.getStr());
    }

    @Override
    public void writeMarshallable(Bytes out) {
        out.writeInt(getInt());
        out.writeUTFΔ(getStr());
    }
    @Override
    public void readMarshallable(Bytes in) {
        setInt(in.readInt());
        setStr(in.readUTFΔ());
    }
    @Override
    public void bytes(Bytes bytes, long offset) {
       this._bytes = bytes;
       this._offset = offset;
    }
    @Override
    public Bytes bytes() {
       return _bytes;
    }
    @Override
    public long offset() {
        return _offset;
    }
    @Override
    public int maxSize() {
       return 19;
    }
    public int hashCode() {
        long lhc = longHashCode();
        return (int) ((lhc >>> 32) ^ lhc);
    }

    public long longHashCode() {
        return (calcLongHashCode(getInt())) * 10191 +
            calcLongHashCode(getStr());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupTest.BaseInterface)) return false;
        GroupTest.BaseInterface that = (GroupTest.BaseInterface) o;

        if(!isEqual(getInt(), that.getInt())) return false;
        return isEqual(getStr(), that.getStr());
    }

    public String toString() {
        if (_bytes == null) return "bytes is null";
        StringBuilder sb = new StringBuilder();
        sb.append("DataValueGroupTest.BaseInterface{ ");
            sb.append("int= ").append(getInt());
sb.append(", ")
;            sb.append("str= ").append(getStr());
        sb.append(" }");
        return sb.toString();
    }
}
