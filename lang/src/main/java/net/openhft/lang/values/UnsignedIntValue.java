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

package net.openhft.lang.values;

import net.openhft.lang.model.constraints.Range;

/**
 * User: peter.lawrey
 * Date: 10/10/13
 * Time: 07:19
 */
public interface UnsignedIntValue {
    long getValue();

    void setValue(@Range(min = 0, max = (1L << 32) - 1) long value);

    long addValue(long delta);
}
