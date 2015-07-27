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
package net.openhft.lang;

import java.util.concurrent.locks.LockSupport;

public class Waiters {
    public static final Waiter BUSY_WAITER = new BusyWaiter();
    public static final Waiter YELD_WAITER = new YieldWaiter();
    public static final Waiter PARK_WAITER = new ParkWaiter();

    public static final class BusyWaiter implements Waiter {
        @Override
        public void await() {
        }
    }

    public static final class YieldWaiter implements Waiter {
        @Override
        public void await() {
            Thread.yield();
        }
    }

    public static final class ParkWaiter implements Waiter {
        @Override
        public void await() {
            LockSupport.parkNanos(1L);
        }
    }
}
