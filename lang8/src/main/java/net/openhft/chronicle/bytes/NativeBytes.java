package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;

import static net.openhft.chronicle.bytes.NativeStore.nativeStore;
import static net.openhft.chronicle.bytes.NoBytesStore.NO_BYTES_STORE;

/**
 * Created by peter.lawrey on 24/02/15.
 */
public class NativeBytes extends AbstractBytes {

    NativeBytes(BytesStore store) {
        super(store);
    }

    public static NativeBytes nativeBytes() {
        return new NativeBytes(NO_BYTES_STORE);
    }

    public static NativeBytes nativeBytes(long initialCapacity) {
        return new NativeBytes(nativeStore(initialCapacity));
    }

    @Override
    protected long checkOffset(long offset, int adding) {
        if (!bytesStore.inStore(offset)) {
            resize(offset);
        }
        return offset;
    }

    private void resize(long offset) {
        if (offset < 0)
            throw new IllegalArgumentException();
        // grow by 50% rounded up to the next pages size
        long ps = OS.pageSize();
        long size = (Math.max(offset, bytesStore.maximumLimit() * 3 / 2) + ps) & ~(ps - 1);
        NativeStore store = NativeStore.lazyNativeStore(size);
        bytesStore.copyTo(store);
        bytesStore.release();
        bytesStore = store;
    }

    @Override
    public long maximumLimit() {
        return 1L << 40;
    }
}
