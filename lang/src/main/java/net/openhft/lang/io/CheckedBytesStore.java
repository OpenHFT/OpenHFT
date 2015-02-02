package net.openhft.lang.io;

import net.openhft.lang.io.serialization.ObjectSerializer;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks if the buffer has been closed, if the buffer is accessed after it is closed an exception
 * is thrown. This class should be used to investigate JVM crashes, it should not be used in
 * production code ( as its slow )
 *
 * Created by Rob Austin
 */
public class CheckedBytesStore implements BytesStore {

    private final BytesStore bytesStore;
    private AtomicBoolean isClosed = new AtomicBoolean();

    Bytes proxy;

    public CheckedBytesStore(final BytesStore bytesStore) {
        this.bytesStore = bytesStore;


        if (bytesStore.bytes() instanceof NativeBytes)
            proxy = new CheckedNativeBytes((NativeBytes) bytesStore.bytes());
        else
            proxy = new CheckedBytes(bytesStore.bytes());
    }

    @Override
    public Bytes bytes() {
        return proxy;
    }

    @Override
    public Bytes bytes(final long offset, final long length) {


        if (bytesStore.bytes() instanceof NativeBytes)
            return new CheckedNativeBytes((NativeBytes) bytesStore.bytes(offset, length));
        else
            return new CheckedBytes(bytesStore.bytes(offset, length));


    }


    @Override
    public long address() {
        if (isClosed.get()) {
            new IllegalStateException(Thread.currentThread().getName() + " called after the byteStore has been freed.").printStackTrace();
            System.exit(-1);
        }
        return bytesStore.address();
    }

    @Override
    public long size() {
        if (isClosed.get()) {
            new IllegalStateException(Thread.currentThread().getName() + " called after the byteStore has been freed.").printStackTrace();
            System.exit(-1);
        }
        return bytesStore.size();
    }

    @Override
    public void free() {
        isClosed.set(true);
        bytesStore.free();
    }

    @Override
    public ObjectSerializer objectSerializer() {
        return bytesStore.objectSerializer();
    }

    @Override
    public File file() {
        return bytesStore.file();
    }


}

