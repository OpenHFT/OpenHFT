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

/**
 * This is cache for the generated classes for a ClassLoader.
 */
class DataValueClassCache {
    private final DataValueGenerator dvg = new DataValueGenerator();

    public <T> T newInstance(Class<T> interfaceClass) {
        try {
            //noinspection ClassNewInstance
            return heapClassFor(interfaceClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T newDirectReference(Class<T> interfaceClass) {
        try {
            //noinspection ClassNewInstance
            return directClassFor(interfaceClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized <T> Class<T> heapClassFor(Class<T> interfaceClass) {
        return dvg.acquireHeapClass(interfaceClass);
    }

    public synchronized <T> Class<T> directClassFor(Class<T> interfaceClass) {
        return dvg.acquireNativeClass(interfaceClass);
    }
}
