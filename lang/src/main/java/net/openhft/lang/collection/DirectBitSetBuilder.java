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

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;

public class DirectBitSetBuilder {
    private boolean assertions;
    private boolean threadSafe;

    public DirectBitSetBuilder() {
        threadSafe = true;
        assertions = false;
        //noinspection ConstantConditions,AssertWithSideEffects
        assert assertions = true;
    }

    public DirectBitSetBuilder assertions(boolean assertions) {
        this.assertions = assertions;
        return this;
    }

    public boolean assertions() {
        return assertions;
    }

    public DirectBitSetBuilder threadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
        return this;
    }

    public boolean threadSafe() {
        return threadSafe;
    }

    public DirectBitSet create(long size) {
        return wrap(DirectStore.allocate((size + 7) >>> 3).bytes());
    }

    static DirectBitSet wrap(Bytes bytes) {
        return new ATSDirectBitSet(bytes);
    }
}
