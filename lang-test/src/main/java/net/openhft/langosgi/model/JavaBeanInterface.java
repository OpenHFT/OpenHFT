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

package net.openhft.langosgi.model;

import net.openhft.lang.model.constraints.MaxSize;

/**
 * User: peter.lawrey Date: 06/10/13 Time: 16:59
 */
public interface JavaBeanInterface {
    int getRecord();

    void setRecord(int record);

    void busyLockRecord() throws InterruptedException;

    void unlockRecord();

    void setFlag(boolean flag);

    boolean getFlag();

    void setByte(byte b);

    byte getByte();

    void setShort(short s);

    short getShort();

    void setChar(char ch);

    char getChar();

    void setInt(int i);

    int getInt();

    void setFloat(float f);

    float getFloat();

    void setLong(long l);

    long getLong();

    void setDouble(double d);

    double getDouble();

    void setString(@MaxSize(8) String s);

    String getString();
}
