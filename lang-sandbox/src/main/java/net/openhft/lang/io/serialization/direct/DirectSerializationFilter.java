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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.openhft.lang.io.serialization.direct.FieldMetadata.*;

final class DirectSerializationFilter {
    public static List<Field> stopAtFirstIneligibleField(List<Field> fields) {
        ArrayList<Field> eligibleFields = new ArrayList<Field>();
        for (Field f : fields) {
            if (checkEligible(f)) {
                eligibleFields.add(f);

            } else {
                break;
            }
        }

        return eligibleFields.isEmpty() ?
                Collections.<Field>emptyList() :
                eligibleFields;
    }

    private static boolean checkEligible(Field f) {
        return isPrimitive(f) &&
                !isStatic(f) &&
                !isTransient(f);
    }
}