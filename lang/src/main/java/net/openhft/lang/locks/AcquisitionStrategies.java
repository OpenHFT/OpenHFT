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

package net.openhft.lang.locks;

import java.util.concurrent.TimeUnit;

public final class AcquisitionStrategies {

    private static class SpinLoopAcquisitionStrategy<S extends LockingStrategy>
            implements AcquisitionStrategy<S, RuntimeException> {
        private final long durationNanos;

        private SpinLoopAcquisitionStrategy(long duration, TimeUnit unit) {
            durationNanos = unit.toNanos(duration);
        }

        @Override
        public <T> boolean acquire(TryAcquireOperation<? super S> operation, S strategy,
                NativeAtomicAccess<T> access, T t, long offset) {
            if (operation.tryAcquire(strategy, access, t, offset))
                return true;
            long deadLine = System.currentTimeMillis() + durationNanos;
            beforeLoop(strategy, access, t, offset);
            do {
                if (operation.tryAcquire(strategy, access, t, offset))
                    return true;
            } while (deadLine - System.currentTimeMillis() >= 0L); // overflow-cautious
            afterLoop(strategy, access, t, offset);
            return end();
        }

        <T> void beforeLoop(S strategy, NativeAtomicAccess<T> access, T t, long offset) {
        }

        <T> void afterLoop(S strategy, NativeAtomicAccess<T> access, T t, long offset) {
        }

        boolean end() {
            return false;
        }
    }

    public static AcquisitionStrategy<LockingStrategy, RuntimeException> spinLoop(
            long duration, TimeUnit unit) {
        return new SpinLoopAcquisitionStrategy<LockingStrategy>(duration, unit);
    }

    private static class SpinLoopOrFailAcquisitionStrategy<S extends LockingStrategy>
            extends SpinLoopAcquisitionStrategy<S> {

        private SpinLoopOrFailAcquisitionStrategy(long duration, TimeUnit unit) {
            super(duration, unit);
        }

        @Override
        boolean end() {
            throw new RuntimeException("Failed to acquire the lock");
        }
    }

    public static AcquisitionStrategy<LockingStrategy, RuntimeException> spinLoopOrFail(
            long duration, TimeUnit unit) {
        return new SpinLoopOrFailAcquisitionStrategy<LockingStrategy>(duration, unit);
    }

    private static class SpinLoopWriteWithWaitsAcquisitionStrategy
            extends SpinLoopOrFailAcquisitionStrategy<ReadWriteWithWaitsLockingStrategy> {

        private SpinLoopWriteWithWaitsAcquisitionStrategy(long duration, TimeUnit unit) {
            super(duration, unit);
        }

        @Override
        <T> void beforeLoop(ReadWriteWithWaitsLockingStrategy strategy,
                            NativeAtomicAccess<T> access, T t, long offset) {
            strategy.registerWait(access, t, offset);
        }

        @Override
        <T> void afterLoop(ReadWriteWithWaitsLockingStrategy strategy,
                           NativeAtomicAccess<T> access, T t, long offset) {
            strategy.deregisterWait(access, t, offset);
        }
    }

    public static AcquisitionStrategy<ReadWriteWithWaitsLockingStrategy, RuntimeException>
    spinLoopRegisteringWaitOrFail(long duration, TimeUnit unit) {
        return new SpinLoopWriteWithWaitsAcquisitionStrategy(duration, unit);
    }

    private AcquisitionStrategies() {}
}
