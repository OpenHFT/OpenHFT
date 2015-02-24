package net.openhft.chronicle.core;

import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
    }

    public static ReferenceCounter onReleased(Runnable onRelease) {
        return new ReferenceCounter(onRelease);
    }

    public void reserve() {
        for (; ; ) {
            long v = value.get();
            if (v <= 0)
                throw new IllegalStateException("Released");
            if (value.compareAndSet(v, v + 1))
                break;
        }
    }

    public void release() {
        for (; ; ) {
            long v = value.get();
            if (v <= 0)
                throw new IllegalStateException("Released");
            if (value.compareAndSet(v, v - 1)) {
                if (v == 1)
                    onRelease.run();
                break;
            }
        }
    }

    public long get() {
        return value.get();
    }

    public String toString() {
        return Long.toString(value.get());
    }

    public void releaseAll() {
        if (value.get() > 0)
            onRelease.run();
    }
}
