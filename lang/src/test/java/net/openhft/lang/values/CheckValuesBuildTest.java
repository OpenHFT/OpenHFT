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

import net.openhft.lang.model.DataValueGenerator;
import org.junit.Test;

/**
 * User: peter.lawrey
 * Date: 10/10/13
 * Time: 11:38
 */
public class CheckValuesBuildTest {
    @Test
    public void testValuesCompile() throws ClassNotFoundException {
        DataValueGenerator dvg = new DataValueGenerator();
        for (Class clazz : new Class[]{
                IntValue.class,
                ByteValue.class,
                CharValue.class,
                ShortValue.class,
                Int24Value.class,
                FloatValue.class,
                DoubleValue.class,
                LongValue.class,
                StringValue.class,
                BooleanValue.class,
                UnsignedByteValue.class,
                UnsignedIntValue.class,
                UnsignedShortValue.class,
                NestAll.class
        }) {
            System.out.println(dvg.acquireHeapClass(clazz).getName() + " "
                    + dvg.acquireNativeClass(clazz).getName());
        }
    }
}
