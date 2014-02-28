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

/**
 * This is cache for the generated classes for a ClassLoader.
 */
public class DataValueClassCache {
    private final DataValueGenerator dvg = new DataValueGenerator();

    public <T> T newInstance(Class<T> interfaceClass) {
        try {
            return heapClassFor(interfaceClass).newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public <T> T newDirectReference(Class<T> interfaceClass) {
        try {
            return directClassFor(interfaceClass).newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public synchronized <T> Class<T> heapClassFor(Class<T> interfaceClass) {
        return dvg.acquireHeapClass(interfaceClass);
    }

    public synchronized <T> Class<T> directClassFor(Class<T> interfaceClass) {
        return dvg.acquireNativeClass(interfaceClass);
    }
}
