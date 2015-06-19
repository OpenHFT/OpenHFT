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
 * Time: 13:26
 */
public interface HugeQueue<T> {
    /**
     * @return is full
     */
    boolean isFull();

    /**
     * @return is empty
     */
    boolean isEmpty();

    /**
     * Add to the end of a queue or return false if full.
     *
     * @return an element to populate or null if full
     */
    T offer();

    /**
     * Add to the end of a queue or return false if full.
     *
     * @param element to add
     * @return true if added or false if full.
     */
    boolean offer(T element);

    /**
     * @return the element or null is non is available
     */
    T take();

    /**
     * Copy data to an element
     *
     * @param element to copy to
     * @return true if one was available or false if not.
     */
    boolean takeCopy(T element);

    /**
     * Recycle an element reference.
     *
     * @param element reference to recycle.
     */
    void recycle(T element);
}
