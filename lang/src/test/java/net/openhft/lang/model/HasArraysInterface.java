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

package net.openhft.lang.model;

import net.openhft.lang.model.constraints.MaxSize;

public interface HasArraysInterface {
    void setFlagAt(@MaxSize(4) int idx, boolean flag);

    boolean getFlagAt(int idx);

    void setByteAt(@MaxSize(4) int idx, byte b);

    byte getByteAt(int idx);

    void setShortAt(@MaxSize(4) int idx, short s);

    short getShortAt(int idx);

    void setCharAt(@MaxSize(4) int idx, char ch);

    char getCharAt(int idx);

    void setIntAt(@MaxSize(4) int idx, int i);

    int getIntAt(int idx);

    void setFloatAt(@MaxSize(4) int idx, float f);

    float getFloatAt(int idx);

    void setLongAt(@MaxSize(4) int idx, long l);

    long getLongAt(int idx);

    void setDoubleAt(@MaxSize(4) int idx, double d);

    double getDoubleAt(int idx);

    void setStringAt(@MaxSize(4) int idx, @MaxSize(8) String s);

    String getStringAt(int idx);
}
