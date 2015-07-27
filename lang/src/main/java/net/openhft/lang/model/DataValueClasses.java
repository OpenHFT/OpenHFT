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

import net.openhft.lang.io.DirectStore;

import java.util.WeakHashMap;

/**
 * This class is a central access point for loading generated on-heap and off heap collections.
 */
public enum DataValueClasses {
    ;
    // the weak hash map is required as the class loader could go away without notice e.g. in OSGi
    private static final WeakHashMap<ClassLoader, DataValueClassCache> cacheMap = new WeakHashMap<ClassLoader, DataValueClassCache>();

    public static <T> T newInstance(Class<T> interfaceClass) {
        DataValueClassCache dataValueClassCache = acquireCache(interfaceClass);
        return dataValueClassCache.newInstance(interfaceClass);
    }

    public static <T> T newDirectReference(Class<T> interfaceClass) {
        DataValueClassCache dataValueClassCache = acquireCache(interfaceClass);
        return dataValueClassCache.newDirectReference(interfaceClass);
    }

    public static <T> T newDirectInstance(Class<T> interfaceClass) {
        T t = newDirectReference(interfaceClass);
        Byteable b = (Byteable) t;
        b.bytes(DirectStore.allocate(b.maxSize()).bytes(), 0);
        return t;
    }

    public static <T> Class<T> heapClassFor(Class<T> interfaceClass) {
        DataValueClassCache dataValueClassCache = acquireCache(interfaceClass);
        return dataValueClassCache.heapClassFor(interfaceClass);
    }

    public static <T> Class<T> directClassFor(Class<T> interfaceClass) {
        DataValueClassCache dataValueClassCache = acquireCache(interfaceClass);
        return dataValueClassCache.directClassFor(interfaceClass);
    }

    private static <T> DataValueClassCache acquireCache(Class<T> interfaceClass) {
        ClassLoader classLoader = interfaceClass.getClassLoader();
        DataValueClassCache dataValueClassCache;
        synchronized (cacheMap) {
            dataValueClassCache = cacheMap.get(classLoader);
            if (dataValueClassCache == null)
                cacheMap.put(classLoader, dataValueClassCache = new DataValueClassCache());
        }
        return dataValueClassCache;
    }
}
