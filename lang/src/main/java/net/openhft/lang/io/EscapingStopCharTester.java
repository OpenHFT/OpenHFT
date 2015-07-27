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
 * Created by peter.lawrey on 16/01/15.
 */
public class EscapingStopCharTester implements StopCharTester {
    private final StopCharTester sct;
    private boolean escaped = false;

    EscapingStopCharTester(StopCharTester sct) {
        this.sct = sct;
    }

    public static StopCharTester escaping(StopCharTester sct) {
        return new EscapingStopCharTester(sct);
    }

    @Override
    public boolean isStopChar(int ch) throws IllegalStateException {
        if (escaped) {
            escaped = false;
            return false;
        }
        if (ch == '\\') {
            escaped = true;
            return false;
        }
        return sct.isStopChar(ch);
    }
}
