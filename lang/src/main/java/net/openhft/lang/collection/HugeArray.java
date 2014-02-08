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

/**
 * User: peter.lawrey
 * Date: 08/10/13
 * Time: 07:40
 */
public interface HugeArray<T> {
    /**
     * @return the capacity of the Array.
     */
    long length();

    /**
     * Get a recycled object which is a reference to this element.
     *
     * @param index to look up.
     * @return object reference to the element.
     */
    T get(long index);

    /**
     * If the element was returned by get(long), re-index it otherwise, get a copy of the object in the array.
     *
     * @param index   of element to copy
     * @param element Copyable element to copy to.
     */
    void get(long index, T element);

    /**
     * Copy the contents of an index to another object
     *
     * @param index to copy
     * @param to    object
     */
    void copyTo(long index, T to);

    /**
     * Set the data in the array to a copy of this element
     *
     * @param index to copy to
     * @param t     to copy
     */
    void set(long index, T t);

    /**
     * recycle the reference,
     *
     * @param t reference to recycle.
     */
    void recycle(T t);
}
