package net.openhft.lang.io;

import java.util.concurrent.TimeUnit;

/**
* Created by peter on 03/08/14.
*/
public interface OffHeapLock {
    boolean tryLock();

    void busyLock();

    boolean busyLock(long time, TimeUnit timeUnit);

    void unlock();
}
