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

package net.openhft.chronicle.bytes;

/**
 * @author peter.lawrey
 */
public interface StopCharTester {
    /**
     * Detect which byte stops the string to be parsed
     *
     * <p>This should be changed to support char instead.
     * 
     * <p>Note: for safety reasons, you should stop on a 0 byte or throw an IllegalStateException.
     *
     * @param ch to test, 0 should return true or throw an exception.
     * @return if this byte is a stop character.
     * @throws IllegalStateException if an invalid character like 0 was detected.
     */
    boolean isStopChar(int ch) throws IllegalStateException;
}
