/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
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
import net.openhft.lang.values.IntValue;
import net.openhft.lang.values.LongValue;
import org.junit.Test;

import static net.openhft.lang.model.DataValueGenerator.firstPrimitiveFieldType;
import static org.junit.Assert.assertEquals;

public class FirstPrimitiveFieldTest {

    @Test
    public void firstPrimitiveFieldTest() {
        assertEquals(int.class, firstPrimitiveFieldType(IntValue.class));
        assertEquals(long.class, firstPrimitiveFieldType(LongValue.class));
        assertEquals(long.class,
                firstPrimitiveFieldType(DataValueClasses.directClassFor(LongValue.class)));
        assertEquals(long.class, firstPrimitiveFieldType(FiveLongValues.class));
        assertEquals(boolean.class, firstPrimitiveFieldType(FiveBooleanValues.class));
        assertEquals(long.class, firstPrimitiveFieldType(FiveLongAndBooleanValues.class));
    }
}

interface FiveLongValues {
    void setValueAt(@MaxSize(5) int i, long v);
    long getValueAt(int i);
}

interface FiveBooleanValues {
    void setValueAt(@MaxSize(5) int i, boolean v);
    boolean getValueAt(int i);
}

interface FiveLongAndBooleanValues {
    void setLongValues(FiveLongValues values);
    FiveLongValues getLongValues();
    void setBooleanValues(FiveBooleanValues values);
    FiveBooleanValues getBooleanValues();
}
