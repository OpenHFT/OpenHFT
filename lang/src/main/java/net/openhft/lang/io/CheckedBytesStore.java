package net.openhft.lang.io;

import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.model.constraints.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;
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
/*
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


    }*/

    @Override
    public Bytes bytes(final long offset, final long length) {

        return bytesStore.bytes(offset, length);
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

    class NativeBytes0 implements NativeBytesI {

        @Override
        public NativeBytesI slice() {
            return null;
        }

        @Override
        public NativeBytesI slice(long offset, long length) {
            return null;
        }

        @Override
        public String toDebugString() {
            return null;
        }

        @Override
        public String toDebugString(long limit) {
            return null;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public char charAt(int index) {
            return 0;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return null;
        }

        @Override
        public NativeBytesI bytes() {
            return null;
        }

        @Override
        public NativeBytesI bytes(long offset, long length) {
            return null;
        }

        @Override
        public long address() {
            return 0;
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public void free() {

        }

        @Override
        public File file() {
            return null;
        }

        @Override
        public Bytes zeroOut() {
            return null;
        }

        @Override
        public Bytes zeroOut(long start, long end) {
            return null;
        }

        @Override
        public Bytes zeroOut(long start, long end, boolean ifNotZero) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public int read(@NotNull byte[] bytes, int off, int len) {
            return 0;
        }

        @Override
        public byte readByte() {
            return 0;
        }

        @Override
        public byte readByte(long offset) {
            return 0;
        }

        @Override
        public int readUnsignedByte() {
            return 0;
        }

        @Override
        public int readUnsignedByte(long offset) {
            return 0;
        }

        @Override
        public void readFully(@NotNull byte[] bytes) {

        }

        @Override
        public void readFully(@NotNull byte[] b, int off, int len) {

        }

        @Override
        public void readFully(long offset, byte[] bytes, int off, int len) {

        }

        @Override
        public void readFully(@NotNull char[] data) {

        }

        @Override
        public void readFully(@NotNull char[] data, int off, int len) {

        }

        @Override
        public int skipBytes(int n) {
            return 0;
        }

        @Override
        public boolean readBoolean() {
            return false;
        }

        @Override
        public boolean readBoolean(long offset) {
            return false;
        }

        @Override
        public short readShort() {
            return 0;
        }

        @Override
        public short readShort(long offset) {
            return 0;
        }

        @Override
        public int readUnsignedShort() {
            return 0;
        }

        @Override
        public int readUnsignedShort(long offset) {
            return 0;
        }

        @Override
        public short readCompactShort() {
            return 0;
        }

        @Override
        public int readCompactUnsignedShort() {
            return 0;
        }

        @Override
        public char readChar() {
            return 0;
        }

        @Override
        public char readChar(long offset) {
            return 0;
        }

        @Override
        public int readInt24() {
            return 0;
        }

        @Override
        public int readInt24(long offset) {
            return 0;
        }

        @Override
        public int readInt() {
            return 0;
        }

        @Override
        public int readInt(long offset) {
            return 0;
        }

        @Override
        public int readVolatileInt() {
            return 0;
        }

        @Override
        public int readVolatileInt(long offset) {
            return 0;
        }

        @Override
        public long readUnsignedInt() {
            return 0;
        }

        @Override
        public long readUnsignedInt(long offset) {
            return 0;
        }

        @Override
        public int readCompactInt() {
            return 0;
        }

        @Override
        public long readCompactUnsignedInt() {
            return 0;
        }

        @Override
        public long readLong() {
            return 0;
        }

        @Override
        public long readLong(long offset) {
            return 0;
        }

        @Override
        public long readVolatileLong() {
            return 0;
        }

        @Override
        public long readVolatileLong(long offset) {
            return 0;
        }

        @Override
        public long readInt48() {
            return 0;
        }

        @Override
        public long readInt48(long offset) {
            return 0;
        }

        @Override
        public long readCompactLong() {
            return 0;
        }

        @Override
        public long readStopBit() {
            return 0;
        }

        @Override
        public float readFloat() {
            return 0;
        }

        @Override
        public float readFloat(long offset) {
            return 0;
        }

        @Override
        public float readVolatileFloat(long offset) {
            return 0;
        }

        @Override
        public double readDouble() {
            return 0;
        }

        @Override
        public double readDouble(long offset) {
            return 0;
        }

        @Override
        public double readCompactDouble() {
            return 0;
        }

        @Override
        public double readVolatileDouble(long offset) {
            return 0;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public String readLine() {
            return null;
        }

        @NotNull
        @Override
        public String readUTF() {
            return null;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public String readUTFΔ() {
            return null;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public String readUTFΔ(long offset) throws IllegalStateException {
            return null;
        }

        @Override
        public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
            return false;
        }

        @Override
        public void read(@NotNull ByteBuffer bb) {

        }

        @org.jetbrains.annotations.Nullable
        @Override
        public <E> E readEnum(@NotNull Class<E> eClass) {
            return null;
        }

        @Override
        public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {

        }

        @Override
        public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
            return null;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public Object readObject() throws IllegalStateException {
            return null;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public <T> T readObject(Class<T> tClass) throws IllegalStateException {
            return null;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
            return null;
        }

        @Override
        public int read() {
            return 0;
        }

        @Override
        public int read(@NotNull byte[] bytes) {
            return 0;
        }

        @Override
        public void write(int b) {

        }

        @Override
        public void writeByte(int v) {

        }

        @Override
        public void writeUnsignedByte(int v) {

        }

        @Override
        public void writeByte(long offset, int b) {

        }

        @Override
        public void writeUnsignedByte(long offset, int v) {

        }

        @Override
        public void write(byte[] bytes) {

        }

        @Override
        public void write(long offset, @NotNull byte[] bytes) {

        }

        @Override
        public void write(byte[] bytes, int off, int len) {

        }

        @Override
        public void write(long offset, byte[] bytes, int off, int len) {

        }

        @Override
        public void write(@NotNull char[] data) {

        }

        @Override
        public void write(@NotNull char[] data, int off, int len) {

        }

        @Override
        public void writeBoolean(boolean v) {

        }

        @Override
        public void writeBoolean(long offset, boolean v) {

        }

        @Override
        public void writeShort(int v) {

        }

        @Override
        public void writeShort(long offset, int v) {

        }

        @Override
        public void writeUnsignedShort(int v) {

        }

        @Override
        public void writeUnsignedShort(long offset, int v) {

        }

        @Override
        public void writeCompactShort(int v) {

        }

        @Override
        public void writeCompactUnsignedShort(int v) {

        }

        @Override
        public void writeChar(int v) {

        }

        @Override
        public void writeChar(long offset, int v) {

        }

        @Override
        public void writeInt24(int v) {

        }

        @Override
        public void writeInt24(long offset, int v) {

        }

        @Override
        public void writeInt(int v) {

        }

        @Override
        public void writeInt(long offset, int v) {

        }

        @Override
        public void writeUnsignedInt(long v) {

        }

        @Override
        public void writeUnsignedInt(long offset, long v) {

        }

        @Override
        public void writeCompactInt(int v) {

        }

        @Override
        public void writeCompactUnsignedInt(long v) {

        }

        @Override
        public void writeOrderedInt(int v) {

        }

        @Override
        public void writeOrderedInt(long offset, int v) {

        }

        @Override
        public boolean compareAndSwapInt(long offset, int expected, int x) {
            return false;
        }

        @Override
        public int getAndAdd(long offset, int delta) {
            return 0;
        }

        @Override
        public int addAndGetInt(long offset, int delta) {
            return 0;
        }

        @Override
        public void writeInt48(long v) {

        }

        @Override
        public void writeInt48(long offset, long v) {

        }

        @Override
        public void writeLong(long v) {

        }

        @Override
        public void writeLong(long offset, long v) {

        }

        @Override
        public void writeCompactLong(long v) {

        }

        @Override
        public void writeOrderedLong(long v) {

        }

        @Override
        public void writeOrderedLong(long offset, long v) {

        }

        @Override
        public boolean compareAndSwapLong(long offset, long expected, long x) {
            return false;
        }

        @Override
        public boolean compareAndSwapDouble(long offset, double expected, double x) {
            return false;
        }

        @Override
        public void writeStopBit(long n) {

        }

        @Override
        public void writeFloat(float v) {

        }

        @Override
        public void writeFloat(long offset, float v) {

        }

        @Override
        public void writeOrderedFloat(long offset, float v) {

        }

        @Override
        public void writeDouble(double v) {

        }

        @Override
        public void writeDouble(long offset, double v) {

        }

        @Override
        public void writeCompactDouble(double v) {

        }

        @Override
        public void writeOrderedDouble(long offset, double v) {

        }

        @Override
        public void writeBytes(@NotNull String s) {

        }

        @Override
        public void writeChars(@NotNull String s) {

        }

        @Override
        public void writeChars(@NotNull CharSequence cs) {

        }

        @Override
        public void writeUTF(@NotNull String s) {

        }

        @Override
        public void writeUTFΔ(@org.jetbrains.annotations.Nullable CharSequence s) throws IllegalArgumentException {

        }

        @Override
        public void writeUTFΔ(long offset, int maxSize, @org.jetbrains.annotations.Nullable CharSequence s) throws IllegalStateException {

        }

        @Override
        public void write(@NotNull ByteBuffer bb) {

        }

        @Override
        public <E> void writeEnum(@org.jetbrains.annotations.Nullable E e) {

        }

        @Override
        public <E> void writeList(@NotNull Collection<E> list) {

        }

        @Override
        public <K, V> void writeMap(@NotNull Map<K, V> map) {

        }

        @Override
        public void writeObject(@org.jetbrains.annotations.Nullable Object object) {

        }

        @Override
        public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {

        }

        @Override
        public void readObject(Object object, int start, int end) {

        }

        @Override
        public long skip(long n) {
            return 0;
        }

        @Override
        public int available() {
            return 0;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean startsWith(RandomDataInput input) {
            return false;
        }

        @Override
        public void writeObject(Object object, int start, int end) {

        }

        @Override
        public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
            return false;
        }

        @Override
        public long position() {
            return 0;
        }

        @Override
        public NativeBytesI position(long position) {
            return null;
        }

        @Override
        public NativeBytesI lazyPosition(long position) {
            return null;
        }

        @Override
        public void write(RandomDataInput bytes) {

        }

        @Override
        public void write(RandomDataInput bytes, long position, long length) {

        }

        @Override
        public long capacity() {
            return 0;
        }

        @Override
        public long remaining() {
            return 0;
        }

        @Override
        public void finish() throws IndexOutOfBoundsException {

        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public Bytes clear() {
            return null;
        }

        @Override
        public Bytes flip() {
            return null;
        }

        @Override
        public long limit() {
            return 0;
        }

        @Override
        public NativeBytesI limit(long limit) {
            return null;
        }

        @NotNull
        @Override
        public ByteOrder byteOrder() {
            return null;
        }

        @Override
        public InputStream inputStream() {
            return null;
        }

        @Override
        public OutputStream outputStream() {
            return null;
        }

        @Override
        public ObjectSerializer objectSerializer() {
            return null;
        }

        @Override
        public void checkEndOfBuffer() throws IndexOutOfBoundsException {

        }

        @Override
        public long startAddr() {
            return 0;
        }

        @Override
        public Bytes load() {
            return null;
        }

        @Override
        public void toString(Appendable sb, long start, long position, long end) {

        }

        @Override
        public void alignPositionAddr(int powerOf2) {

        }

        @Override
        public void positionAddr(long positionAddr) {

        }

        @Override
        public long positionAddr() {
            return 0;
        }

        @Override
        public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
            return null;
        }

        @Override
        public void clearThreadAssociation() {

        }

        @Override
        public ByteStringAppender append(@net.openhft.lang.model.constraints.NotNull CharSequence s) {
            return null;
        }

        @Override
        public ByteStringAppender append(@net.openhft.lang.model.constraints.NotNull CharSequence s, int start, int end) {
            return null;
        }

        @Override
        public ByteStringAppender append(boolean b) {
            return null;
        }

        @Override
        public ByteStringAppender append(char c) {
            return null;
        }

        @Override
        public ByteStringAppender append(@Nullable Enum value) {
            return null;
        }

        @Override
        public ByteStringAppender append(int i) {
            return null;
        }

        @Override
        public ByteStringAppender append(long l) {
            return null;
        }

        @Override
        public ByteStringAppender append(long l, int base) {
            return null;
        }

        @Override
        public ByteStringAppender appendTimeMillis(long timeInMS) {
            return null;
        }

        @Override
        public ByteStringAppender appendDateMillis(long timeInMS) {
            return null;
        }

        @Override
        public ByteStringAppender appendDateTimeMillis(long timeInMS) {
            return null;
        }

        @Override
        public ByteStringAppender append(double d) {
            return null;
        }

        @Override
        public ByteStringAppender append(double d, int precision) {
            return null;
        }

        @Override
        public ByteStringAppender append(@net.openhft.lang.model.constraints.NotNull MutableDecimal md) {
            return null;
        }

        @Override
        public <E> ByteStringAppender append(@net.openhft.lang.model.constraints.NotNull Iterable<E> list, @net.openhft.lang.model.constraints.NotNull CharSequence separator) {
            return null;
        }

        @Override
        public void selfTerminating(boolean selfTerminate) {

        }

        @Override
        public boolean selfTerminating() {
            return false;
        }

        @Override
        public int readUnsignedByteOrThrow() throws BufferUnderflowException {
            return 0;
        }

        @Override
        public Boolean parseBoolean(@net.openhft.lang.model.constraints.NotNull StopCharTester tester) throws BufferUnderflowException {
            return null;
        }

        @Override
        public void parseUTF(@net.openhft.lang.model.constraints.NotNull StringBuilder builder, @net.openhft.lang.model.constraints.NotNull StopCharTester tester) throws BufferUnderflowException {

        }

        @Override
        public String parseUTF(@net.openhft.lang.model.constraints.NotNull StopCharTester tester) throws BufferUnderflowException {
            return null;
        }

        @Override
        public <E extends Enum<E>> E parseEnum(@net.openhft.lang.model.constraints.NotNull Class<E> eClass, @net.openhft.lang.model.constraints.NotNull StopCharTester tester) throws BufferUnderflowException {
            return null;
        }

        @Override
        public MutableDecimal parseDecimal(@net.openhft.lang.model.constraints.NotNull MutableDecimal decimal) throws BufferUnderflowException {
            return null;
        }

        @Override
        public long parseLong() throws BufferUnderflowException {
            return 0;
        }

        @Override
        public long parseLong(int base) throws BufferUnderflowException {
            return 0;
        }

        @Override
        public double parseDouble() throws BufferUnderflowException {
            return 0;
        }

        @Override
        public boolean stepBackAndSkipTo(@net.openhft.lang.model.constraints.NotNull StopCharTester tester) {
            return false;
        }

        @Override
        public boolean skipTo(@net.openhft.lang.model.constraints.NotNull StopCharTester tester) {
            return false;
        }

        @Override
        public void asString(Appendable appendable) {

        }

        @Override
        public CharSequence asString() {
            return null;
        }

        @Override
        public void readMarshallable(@net.openhft.lang.model.constraints.NotNull Bytes in) throws IllegalStateException {

        }

        @Override
        public void writeMarshallable(@net.openhft.lang.model.constraints.NotNull Bytes out) {

        }

        @Override
        public byte addByte(long offset, byte b) {
            return 0;
        }

        @Override
        public int addUnsignedByte(long offset, int i) {
            return 0;
        }

        @Override
        public short addShort(long offset, short s) {
            return 0;
        }

        @Override
        public int addUnsignedShort(long offset, int i) {
            return 0;
        }

        @Override
        public int addInt(long offset, int i) {
            return 0;
        }

        @Override
        public long addUnsignedInt(long offset, long i) {
            return 0;
        }

        @Override
        public long addLong(long offset, long i) {
            return 0;
        }

        @Override
        public float addFloat(long offset, float f) {
            return 0;
        }

        @Override
        public double addDouble(long offset, double d) {
            return 0;
        }

        @Override
        public int addAtomicInt(long offset, int i) {
            return 0;
        }

        @Override
        public long addAtomicLong(long offset, long l) {
            return 0;
        }

        @Override
        public float addAtomicFloat(long offset, float f) {
            return 0;
        }

        @Override
        public double addAtomicDouble(long offset, double d) {
            return 0;
        }

        @Override
        public boolean tryLockInt(long offset) {
            return false;
        }

        @Override
        public boolean tryLockNanosInt(long offset, long nanos) {
            return false;
        }

        @Override
        public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {

        }

        @Override
        public void unlockInt(long offset) throws IllegalMonitorStateException {

        }

        @Override
        public void resetLockInt(long offset) {

        }

        @Override
        public int threadIdForLockInt(long offset) {
            return 0;
        }

        @Override
        public boolean tryLockLong(long offset) {
            return false;
        }

        @Override
        public boolean tryLockNanosLong(long offset, long nanos) {
            return false;
        }

        @Override
        public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {

        }

        @Override
        public void unlockLong(long offset) throws IllegalMonitorStateException {

        }

        @Override
        public void resetLockLong(long offset) {

        }

        @Override
        public long threadIdForLockLong(long offset) {
            return 0;
        }

        @Override
        public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
            return false;
        }

        @Override
        public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
            return false;
        }

        @Override
        public void unlockRWReadLock(long offset) throws IllegalStateException {

        }

        @Override
        public void unlockRWWriteLock(long offset) throws IllegalStateException {

        }

        @Override
        public void reserve() {

        }

        @Override
        public void release() {

        }

        @Override
        public int refCount() {
            return 0;
        }
    }
}

