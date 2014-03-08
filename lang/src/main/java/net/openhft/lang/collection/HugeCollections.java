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

package net.openhft.lang.collection;

import net.openhft.lang.collection.impl.HugeArrayImpl;
import net.openhft.lang.collection.impl.HugeQueueImpl;

/**
 * User: peter.lawrey
 * Date: 08/10/13
 * Time: 08:09
 */
public enum HugeCollections {
    ;

    public static <T> HugeArray<T> newArray(Class<T> tClass, long length) {
        return new HugeArrayImpl<T>(tClass, length);
    }

    public static <T> HugeQueue<T> newQueue(Class<T> tClass, long length) {
        return new HugeQueueImpl<T>(new HugeArrayImpl<T>(tClass, length + 1), length + 1);
    }
}
