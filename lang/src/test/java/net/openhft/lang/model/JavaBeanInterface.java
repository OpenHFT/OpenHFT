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

package net.openhft.lang.model;

import net.openhft.lang.model.constraints.MaxSize;

/**
 * User: plawrey
 * Date: 06/10/13
 * Time: 16:59
 */
public interface JavaBeanInterface {
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
