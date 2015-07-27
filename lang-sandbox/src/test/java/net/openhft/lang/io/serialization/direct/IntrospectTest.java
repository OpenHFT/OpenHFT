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

package net.openhft.lang.io.serialization.direct;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.openhft.lang.io.serialization.direct.TestClasses.*;
import static org.junit.Assert.assertEquals;

public class IntrospectTest {

    @Test
    public void returnsFieldsRegardlessOfVisibility() {
        List<Field> fields = asFieldList(InstanceOnlyNoStaticFields.class);

        assertEquals(3, fields.size());
    }

    @Test
    public void returnsStaticAsWellAsInstanceFields() {
        List<Field> fields = asFieldList(HasInstanceAndStaticFields.class);

        assertEquals(6, fields.size());
    }

    @Test
    public void walksClassHierarchyForAllFields() {
        List<Field> fields = asFieldList(LevelTwoDerivedClass.class);

        assertEquals(6, fields.size());
    }

    private static List<Field> asFieldList(Class<?> clazz) {
        return new ArrayList<Field>(Introspect.fields(clazz));
    }
}

