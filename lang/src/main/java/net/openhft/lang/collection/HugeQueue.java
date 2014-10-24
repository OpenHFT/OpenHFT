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
