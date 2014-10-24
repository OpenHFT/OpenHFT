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
