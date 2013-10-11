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
