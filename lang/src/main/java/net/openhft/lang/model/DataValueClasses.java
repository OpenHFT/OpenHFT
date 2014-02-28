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

package net.openhft.lang.model;

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
