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

import net.openhft.lang.io.serialization.ObjectSerializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unlike {@link NativeBytes}, always throw check bounds and exceptions on
 * all write methods, including that write a single primitive, e. g. {@link #writeInt(int)}.
 * {@code NativeBytes} throw exceptions only if Java assertions enabled.
 */
public class BoundsCheckingNativeBytes extends NativeBytes {

    public BoundsCheckingNativeBytes(long startAddr, long capacityAddr) {
        super(startAddr, capacityAddr);
    }

    public BoundsCheckingNativeBytes(ObjectSerializer objectSerializer,
                                     long startAddr, long capacityAddr,
                                     AtomicInteger refCount) {
        super(objectSerializer, startAddr, capacityAddr, refCount);
    }

    public BoundsCheckingNativeBytes(NativeBytes bytes) {
        super(bytes);
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
