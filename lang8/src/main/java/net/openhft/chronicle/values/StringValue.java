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

package net.openhft.chronicle.values;

/**
 * User: peter.lawrey Date: 10/10/13 Time: 07:13
 */
public interface StringValue {

    String getValue();

    void setValue(@MaxBytes CharSequence value);

    /**
     * a getter for a String which takes a StringBuilder
     *
     * @param stringBuilder the builder to return
     * @return a StringBuilder containing the value
     */
    StringBuilder getUsingValue(StringBuilder stringBuilder);
}
