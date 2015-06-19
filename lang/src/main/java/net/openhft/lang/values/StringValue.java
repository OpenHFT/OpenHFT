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

import net.openhft.lang.model.constraints.MaxSize;

/**
 * User: peter.lawrey Date: 10/10/13 Time: 07:13
 */
public interface StringValue {

    String getValue();

    void setValue(@MaxSize CharSequence value);

    /**
     * a getter for a String which takes a StringBuilder
     * @param stringBuilder the builder to return
     * @return a StringBuilder containing the value
     */
    StringBuilder getUsingValue(StringBuilder stringBuilder);
}
