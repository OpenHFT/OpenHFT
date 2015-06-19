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

package net.openhft.lang.io.serialization.impl;

/**
 * Created by peter.lawrey on 29/10/14.
 */
public class StringBuilderPool {
    final ThreadLocal<StringBuilder> sbtl = new ThreadLocal<StringBuilder>();

    public StringBuilder acquireStringBuilder() {
        StringBuilder sb = sbtl.get();
        if (sb == null) {
            sbtl.set(sb = new StringBuilder(128));
        }
        sb.setLength(0);
        return sb;
    }
}
