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

import java.util.LinkedList;
import java.util.List;

public class WaitStrategyBuilder {
    private int lowerLimit;
    private List<WaiterHelper> waiters;

    public WaitStrategyBuilder() {
        this.waiters = new LinkedList<WaiterHelper>();
        this.lowerLimit = 0;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public WaitStrategyBuilder busyWaiter(int count) {
        return waiter(Waiters.BUSY_WAITER,count);
    }

    public WaitStrategyBuilder yeldWaiter(int count) {
        return waiter(Waiters.YELD_WAITER,count);
    }

    public WaitStrategyBuilder parkWaiter(int count) {
        return waiter(Waiters.PARK_WAITER,count);
    }

    public WaitStrategyBuilder waiter(Waiter waiter,int count) {
        this.waiters.add(new WaiterHelper(waiter,this.lowerLimit,count));
        this.lowerLimit += count;
        return this;
    }

    public WaitStrategy build() {
        switch(this.waiters.size()) {
            case 1:
                return new WaitStrategy1(this.waiters);
            case 2:
                return new WaitStrategy2(this.waiters);
            case 3:
                return new WaitStrategy3(this.waiters);
        }

        return new WaitStrategyN(this.waiters);
    }

    // *************************************************************************
    //
    // *************************************************************************

    private class WaiterHelper {
        private final Waiter waiter;
        private int min;
        private int max;

        public WaiterHelper(final Waiter waiter) {
            this(waiter,-1,-1);
        }

        public WaiterHelper(final Waiter waiter,int start, int count) {
            this.waiter = waiter;
            this.min = start;
            this.max = start + count;
        }

        public boolean contains(int value) {
            return value >= this.min && value <= this.min;
        }

        public Waiter waiter() {
            return this.waiter;
        }

        public int min() {
            return this.min;
        }

        public int max() {
            return this.max;
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    private class WaitStrategy1 implements WaitStrategy {
        private final Waiter waiter;

        public WaitStrategy1(final List<WaiterHelper> waiters) {
            this.waiter = waiters.get(0).waiter;
        }

        @Override
        public void await(int counter) {
            this.waiter.await();
        }
    }

    private class WaitStrategy2 implements WaitStrategy {
        private final int limit;
        private final Waiter waiter1;
        private final Waiter waiter2;

        public WaitStrategy2(final List<WaiterHelper> waiters) {
            this.limit   = waiters.get(0).max;
            this.waiter1 = waiters.get(0).waiter;
            this.waiter2 = waiters.get(1).waiter;
        }

        @Override
        public void await(int counter) {
            if(counter < this.limit) {
                this.waiter1.await();

            } else {
                this.waiter2.await();
            }
        }
    }

    private class WaitStrategy3 implements WaitStrategy {
        private final int limit1;
        private final int limit2;
        private final Waiter waiter1;
        private final Waiter waiter2;
        private final Waiter waiter3;

        public WaitStrategy3(final List<WaiterHelper> waiters) {
            this.limit1  = waiters.get(0).max;
            this.limit2  = waiters.get(1).max;
            this.waiter1 = waiters.get(0).waiter;
            this.waiter2 = waiters.get(1).waiter;
            this.waiter3 = waiters.get(2).waiter;
        }

        @Override
        public void await(int counter) {
            if(counter < this.limit1) {
                this.waiter1.await();

            } else if(counter < this.limit2) {
                this.waiter2.await();

            } else {
                this.waiter3.await();
            }
        }
    }

    private class WaitStrategyN implements WaitStrategy {
        final List<WaiterHelper> waiters;

        public WaitStrategyN(final List<WaiterHelper> waiters) {
            this.waiters = new LinkedList<WaiterHelper>(waiters);
        }

        @Override
        public void await(int counter) {
            WaiterHelper wh = null;
            for(int i=0;i<this.waiters.size();i++) {
                wh = this.waiters.get(i);
                if(i == this.waiters.size() - 1 || wh.contains(counter)) {
                    wh.waiter().await();
                    break;
                }
            }
        }
    }
}
