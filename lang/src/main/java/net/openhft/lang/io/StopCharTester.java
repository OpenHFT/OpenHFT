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

package net.openhft.lang.io;

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
