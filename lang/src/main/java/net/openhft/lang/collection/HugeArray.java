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
