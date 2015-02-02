package net.openhft.lang.io;

import net.openhft.lang.io.serialization.ObjectSerializer;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks if the buffer has been closed, if the buffer is accessed after it is closed an exception is thrown.
 * This class should be used to investigate JVM crashes, it should not be used in production code ( as its slow )
 *
 * Created by Rob Austin
 */
public class CheckedBytesStore implements BytesStore {

    private final BytesStore bytesStore;
    private AtomicBoolean isClosed = new AtomicBoolean();

    Bytes proxy;

    public CheckedBytesStore(final BytesStore bytesStore) {
        this.bytesStore = bytesStore;
        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (isClosed.get())
                    new IllegalStateException(Thread.currentThread().getName() + " calling " + method + " after the byteStore has been freed.").printStackTrace();
                return method.invoke(bytesStore.bytes(), args);

            }
        };

        if (bytesStore.bytes() instanceof NativeBytesI)
            proxy = (NativeBytesI) Proxy.newProxyInstance(
                    NativeBytesI.class.getClassLoader(),
                    new Class[]{NativeBytesI.class},
                    handler);
        else
            proxy = (Bytes) Proxy.newProxyInstance(
                    Bytes.class.getClassLoader(),
                    new Class[]{Bytes.class},
                    handler);
    }

    @Override
    public Bytes bytes() {
        return proxy;
    }

    @Override
    public Bytes bytes(final long offset, final long length) {

        InvocationHandler handler = new InvocationHandler() {
            Bytes bytes0 = bytesStore.bytes(offset, length);

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                 if (isClosed.get()) {
                    new IllegalStateException(Thread.currentThread().getName() + " calling " + method + " after the byteStore has been freed.").printStackTrace();
                    System.exit(-1);
                }
                return method.invoke(bytes0, args);
            }
        };

        if (bytesStore.bytes() instanceof NativeBytesI)
            return (NativeBytesI) Proxy.newProxyInstance(
                    NativeBytesI.class.getClassLoader(),
                    new Class[]{NativeBytesI.class},
                    handler);
        else
            return (Bytes) Proxy.newProxyInstance(
                    Bytes.class.getClassLoader(),
                    new Class[]{Bytes.class},
                    handler);
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
