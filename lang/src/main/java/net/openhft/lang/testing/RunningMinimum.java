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

package net.openhft.lang.testing;

/**
 * User: peter.lawrey
 * Date: 05/08/13
 * Time: 19:06
 */
public class RunningMinimum implements Differencer {
    private final long actualMinimum;
    private final int drift;
    private long lastStartTime = Long.MIN_VALUE;
    private long minimum = Long.MAX_VALUE;

    public RunningMinimum(long actualMinimum) {
        this(actualMinimum, 100 * 1000);
    }

    private RunningMinimum(long actualMinimum, int drift) {
        this.actualMinimum = actualMinimum;
        this.drift = drift;
    }

    @Override
    public long sample(long startTime, long endTime) {
        if (lastStartTime + drift <= startTime) {
            if (lastStartTime != Long.MIN_VALUE)
                minimum += (startTime - lastStartTime) / drift;
            lastStartTime = startTime;
        }
        long delta = endTime - startTime;
        if (minimum > delta)
            minimum = delta;
        return delta - minimum + actualMinimum;
    }

    public long minimum() {
        return minimum;
    }
}
