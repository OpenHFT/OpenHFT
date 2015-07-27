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
