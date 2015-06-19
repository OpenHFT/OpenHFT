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
