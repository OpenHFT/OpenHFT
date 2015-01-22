package net.openhft.lang.thread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by peter on 11/12/14.
 */
public class LightPauser implements Pauser {
    private final AtomicBoolean pausing = new AtomicBoolean();
    private final int busyCount;
    private final long parkPeriod;
    private int count;
    private volatile Thread thread;

    public LightPauser(int busyCount, long parkPeriod) {
        this.busyCount = busyCount;
        this.parkPeriod = parkPeriod;
    }

    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public void pause() {
        pause(parkPeriod);
    }

    public void pause(long maxPauseNS) {
        if (count++ < busyCount || maxPauseNS < 10000)
            return;
        thread = Thread.currentThread();
        pausing.set(true);
        doPause(maxPauseNS);
        pausing.set(false);
        reset();
    }

    protected void doPause(long maxPauseNS) {
        LockSupport.parkNanos(Math.max(maxPauseNS, parkPeriod));
    }

    @Override
    public void unpause() {
        if (pausing.get())
            LockSupport.unpark(thread);
    }
}
