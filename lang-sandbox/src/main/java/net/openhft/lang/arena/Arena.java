package net.openhft.lang.arena;

import net.openhft.lang.io.NativeBytes;

import java.util.concurrent.TimeUnit;

/**
 * Created by peter on 20/06/14.
 */
public interface Arena {
    /**
     * get the id from the header.
     * @param n
     * @return
     */
    int getHeaderId(int n);
    /**
     * Allocate a new block of memory of at least size.
     *
     * @param size required
     * @return the handle for this block of memory.
     */
    int allocate(long size);

    /**
     * Lookup a handle.
     *
     * @param handle id to look up
     * @param bytes to assign to this region of memory.
     * @return if the handle exists, false if bytes is invalid.
     */
    boolean lookup(int handle, NativeBytes bytes);


    /**
     * delete a handle
     *
     * @param handle to remove
     */
    void remove(int handle);

    /**
     * Mark a handle as dirty.
     *
     * @param handle to mark as dirty
     */
    void dirty(int handle);

    /**
     * lock a whole Arena
     *
     * @throws InterruptedException
     */
    void lock() throws InterruptedException;

    /**
     * Lock a whole arena or give up after a period of time.
     *
     * @param time     to wait
     * @param timeUnit units of time
     * @return true if locked, or false it it failed to obtain the lock
     * @throws InterruptedException
     */
    boolean lock(long time, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Unlock this Arena
     *
     * @throws IllegalMonitorStateException if this thread doesn't hold the lock.
     */
    void unlock() throws IllegalMonitorStateException;

    /**
     * Explicitly clear the lock even if it is held.
     */
    void resetLock();

    /**
     * Arena iterator
     *
     * @return the number to iterator over to get all handles.
     */
    int indexEnd();

    /**
     * Get a handle for an index or 0 if deleted.
     *
     * @param index to lookup to get a handle.
     * @return
     */
    int handleByIndex(int index);
}
