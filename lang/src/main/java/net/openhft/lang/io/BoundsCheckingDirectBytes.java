/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unlike {@link net.openhft.lang.io.NativeBytes}, always throw check bounds and exceptions on all write methods,
 * including that write a single primitive, e. g. {@link #writeInt(int)}. {@code NativeBytes} throw exceptions only if
 * Java assertions enabled.
 */
public class BoundsCheckingDirectBytes extends DirectBytes {

    public BoundsCheckingDirectBytes(@NotNull BytesStore store, AtomicInteger refCount) {
        super(store, refCount);
    }

    @Override
    void positionChecks(long positionAddr) {
        actualPositionChecks(positionAddr);
    }

    @Override
    void offsetChecks(long offset, long len) {
        actualOffsetChecks(offset, len);
    }
}
