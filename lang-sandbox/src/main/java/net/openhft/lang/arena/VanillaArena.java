package net.openhft.lang.arena;

import net.openhft.lang.io.MappedStore;
import net.openhft.lang.io.NativeBytes;

import java.util.concurrent.TimeUnit;

/**
 * Created by peter on 22/06/14.
 */
public class VanillaArena implements Arena {
    static final byte[] MAGIC = "VanArena".getBytes();
    static final int MAGIC_OFFSET = 0;
    static final int LOCK_OFFSET = MAGIC_OFFSET+8;
    static final int ALLOCATE_SIZE_OFFSET = LOCK_OFFSET+8;
    static final int ALLOCATIONS_OFFSET = ALLOCATE_SIZE_OFFSET + 4;
    static final int HEADER_ID_OFFSET0 = 32;
    static final int HEADER_ID_SIZE = 4;
    static final int HEADER_LENGTH = 64;

    final MappedStore store;

    public VanillaArena(MappedStore store) {
        this.store = store;
    }

    @Override
    public int getHeaderId(int n) {
        return 0;
    }

    @Override
    public int allocate(long size) {
        return 0;
    }

    @Override
    public boolean lookup(int handle, NativeBytes bytes) {
        return false;
    }

    @Override
    public void remove(int handle) {

    }

    @Override
    public void dirty(int handle) {

    }

    @Override
    public void lock() throws InterruptedException {

    }

    @Override
    public boolean lock(long time, TimeUnit timeUnit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() throws IllegalMonitorStateException {

    }

    @Override
    public void resetLock() {

    }

    @Override
    public int indexEnd() {
        return 0;
    }

    @Override
    public int handleByIndex(int index) {
        return 0;
    }
}
