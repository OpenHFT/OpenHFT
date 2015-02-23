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
