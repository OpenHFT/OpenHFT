/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
