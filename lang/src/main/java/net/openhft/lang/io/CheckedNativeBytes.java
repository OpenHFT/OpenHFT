package net.openhft.lang.io;

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.pool.StringInterner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rob Austin.
 */
public class CheckedNativeBytes implements Bytes, NativeBytesI {
    private final NativeBytes nativeBytes;

    volatile boolean isClosed = false;

    public CheckedNativeBytes(long startAddr, long capacityAddr) {
        checkNotClosed();
        nativeBytes = new NativeBytes(startAddr, capacityAddr);
    }

    public CheckedNativeBytes(BytesMarshallerFactory bytesMarshallerFactory, long startAddr, long capacityAddr, AtomicInteger refCount) {
        checkNotClosed();
        nativeBytes = new NativeBytes(bytesMarshallerFactory, startAddr, capacityAddr, refCount);
    }

    public CheckedNativeBytes(ObjectSerializer objectSerializer, long startAddr, long capacityAddr, AtomicInteger refCount) {
        checkNotClosed();
        nativeBytes = new NativeBytes(objectSerializer, startAddr, capacityAddr, refCount);
    }

    public CheckedNativeBytes(NativeBytes bytes) {
        checkNotClosed();
        nativeBytes = new NativeBytes((NativeBytes) bytes);
    }

    @Override
    public void write(long offset, Bytes bytes) {
        nativeBytes.write(offset, bytes);
    }

    public void clearThreadAssociation() {
        checkNotClosed();
        nativeBytes.clearThreadAssociation();
    }

    boolean checkSingleThread() {
        checkNotClosed();
        return nativeBytes.checkSingleThread();
    }

    public long size() {
        checkNotClosed();
        return nativeBytes.size();
    }

    void checkNotClosed() {
        if (isClosed) {
            System.err.print("Thread " + Thread.currentThread().getName() + " performing processing " +
                    "after free()");
            ThreadInfo[] threads = ManagementFactory.getThreadMXBean()
                    .dumpAllThreads(true, true);
            for (final ThreadInfo info : threads)
                System.out.print(info);

            System.exit(-1);
        }
    }

    public void free() {
        isClosed = true;
        nativeBytes.free();
    }

    public void reserve() {
        checkNotClosed();
        nativeBytes.reserve();
    }

    public void release() {
        checkNotClosed();
        nativeBytes.release();
    }


    public int refCount() {
        checkNotClosed();
        return nativeBytes.refCount();
    }

    StringInterner stringInterner() {
        checkNotClosed();
        return nativeBytes.stringInterner();
    }

    public void selfTerminating(boolean selfTerminating) {
        checkNotClosed();
        nativeBytes.selfTerminating(selfTerminating);
    }

    public boolean selfTerminating() {
        checkNotClosed();
        return nativeBytes.selfTerminating();
    }

    public int readUnsignedByteOrThrow() throws BufferUnderflowException {
        return nativeBytes.readUnsignedByteOrThrow();
    }

    public int readByteOrThrow(boolean selfTerminating) throws BufferUnderflowException {
        return nativeBytes.readByteOrThrow(selfTerminating);
    }

    public Boolean parseBoolean(@NotNull StopCharTester tester) {
        checkNotClosed();
        return nativeBytes.parseBoolean(tester);
    }

    public void readFully(@NotNull byte[] bytes) {
        checkNotClosed();
        nativeBytes.readFully(bytes);
    }

    public void readFully(@NotNull char[] data) {
        checkNotClosed();
        nativeBytes.readFully(data);
    }

    public int skipBytes(int n) {
        checkNotClosed();
        return nativeBytes.skipBytes(n);
    }

    public boolean readBoolean() {
        checkNotClosed();
        return nativeBytes.readBoolean();
    }

    public boolean readBoolean(long offset) {
        checkNotClosed();
        return nativeBytes.readBoolean(offset);
    }

    public int readUnsignedByte() {
        checkNotClosed();
        return nativeBytes.readUnsignedByte();
    }

    public int readUnsignedByte(long offset) {
        checkNotClosed();
        return nativeBytes.readUnsignedByte(offset);
    }

    public int readUnsignedShort() {
        checkNotClosed();
        return nativeBytes.readUnsignedShort();
    }

    public int readUnsignedShort(long offset) {
        checkNotClosed();
        return nativeBytes.readUnsignedShort(offset);
    }

    @NotNull
    public String readLine() {
        checkNotClosed();
        return nativeBytes.readLine();
    }

    @Nullable
    public String readUTFΔ() {
        checkNotClosed();
        return nativeBytes.readUTFΔ();
    }

    @Nullable
    public String readUTFΔ(long offset) throws IllegalStateException {
        return nativeBytes.readUTFΔ(offset);
    }

    public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
        checkNotClosed();
        return nativeBytes.readUTFΔ(stringBuilder);
    }

    @NotNull
    public String parseUTF(@NotNull StopCharTester tester) {
        checkNotClosed();
        return nativeBytes.parseUTF(tester);
    }

    public void parseUTF(@NotNull StringBuilder builder, @NotNull StopCharTester tester) {
        checkNotClosed();
        nativeBytes.parseUTF(builder, tester);
    }

    public boolean stepBackAndSkipTo(@NotNull StopCharTester tester) {
        checkNotClosed();
        return nativeBytes.stepBackAndSkipTo(tester);
    }

    public boolean skipTo(@NotNull StopCharTester tester) {
        checkNotClosed();
        return nativeBytes.skipTo(tester);
    }

    @NotNull
    public String readUTF() {
        checkNotClosed();
        return nativeBytes.readUTF();
    }

    public short readCompactShort() {
        checkNotClosed();
        return nativeBytes.readCompactShort();
    }

    public int readCompactUnsignedShort() {
        checkNotClosed();
        return nativeBytes.readCompactUnsignedShort();
    }

    public int readInt24() {
        checkNotClosed();
        return nativeBytes.readInt24();
    }

    public int readInt24(long offset) {
        checkNotClosed();
        return nativeBytes.readInt24(offset);
    }

    public long readUnsignedInt() {
        checkNotClosed();
        return nativeBytes.readUnsignedInt();
    }

    public long readUnsignedInt(long offset) {
        checkNotClosed();
        return nativeBytes.readUnsignedInt(offset);
    }

    public int readCompactInt() {
        checkNotClosed();
        return nativeBytes.readCompactInt();
    }

    public long readCompactUnsignedInt() {
        checkNotClosed();
        return nativeBytes.readCompactUnsignedInt();
    }

    public long readInt48() {
        checkNotClosed();
        return nativeBytes.readInt48();
    }

    public long readInt48(long offset) {
        checkNotClosed();
        return nativeBytes.readInt48(offset);
    }

    public long readCompactLong() {
        checkNotClosed();
        return nativeBytes.readCompactLong();
    }

    public long readStopBit() {
        checkNotClosed();
        return nativeBytes.readStopBit();
    }

    public double readCompactDouble() {
        checkNotClosed();
        return nativeBytes.readCompactDouble();
    }

    public void read(@NotNull ByteBuffer bb) {
        checkNotClosed();
        nativeBytes.read(bb);
    }

    public void write(@NotNull byte[] bytes) {
        checkNotClosed();
        nativeBytes.write(bytes);
    }

    public void writeBoolean(boolean v) {
        checkNotClosed();
        nativeBytes.writeBoolean(v);
    }

    public void writeBoolean(long offset, boolean v) {
        checkNotClosed();
        nativeBytes.writeBoolean(offset, v);
    }

    public void writeBytes(@NotNull String s) {
        checkNotClosed();
        nativeBytes.writeBytes(s);
    }

    public void writeChars(@NotNull String s) {
        checkNotClosed();
        nativeBytes.writeChars(s);
    }

    public void writeChars(@NotNull CharSequence cs) {
        checkNotClosed();
        nativeBytes.writeChars(cs);
    }

    public void writeUTF(@NotNull String str) {
        checkNotClosed();
        nativeBytes.writeUTF(str);
    }

    public void writeUTFΔ(@Nullable CharSequence str) throws IllegalArgumentException {
        nativeBytes.writeUTFΔ(str);
    }

    public void writeUTFΔ(long offset, int maxSize, @Nullable CharSequence s) throws IllegalStateException {
        nativeBytes.writeUTFΔ(offset, maxSize, s);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence str) {
        checkNotClosed();
        return nativeBytes.append(str);
    }

    public void writeByte(int v) {
        checkNotClosed();
        nativeBytes.writeByte(v);
    }

    public void writeUnsignedByte(int v) {
        checkNotClosed();
        nativeBytes.writeUnsignedByte(v);
    }

    public void writeUnsignedByte(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeUnsignedByte(offset, v);
    }


    public void write(@NotNull char[] data) {
        checkNotClosed();
        nativeBytes.write(data);
    }

    public void write(@NotNull char[] data, int off, int len) {
        checkNotClosed();
        nativeBytes.write(data, off, len);
    }

    public void writeUnsignedShort(int v) {
        checkNotClosed();
        nativeBytes.writeUnsignedShort(v);
    }

    public void writeUnsignedShort(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeUnsignedShort(offset, v);
    }

    public void writeCompactShort(int v) {
        checkNotClosed();
        nativeBytes.writeCompactShort(v);
    }

    public void writeCompactUnsignedShort(int v) {
        checkNotClosed();
        nativeBytes.writeCompactUnsignedShort(v);
    }

    public void writeInt24(int v) {
        checkNotClosed();
        nativeBytes.writeInt24(v);
    }

    public void writeInt24(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeInt24(offset, v);
    }

    public void writeUnsignedInt(long v) {
        checkNotClosed();
        nativeBytes.writeUnsignedInt(v);
    }

    public void writeUnsignedInt(long offset, long v) {
        checkNotClosed();
        nativeBytes.writeUnsignedInt(offset, v);
    }

    public void writeCompactInt(int v) {
        checkNotClosed();
        nativeBytes.writeCompactInt(v);
    }

    public void writeCompactUnsignedInt(long v) {
        checkNotClosed();
        nativeBytes.writeCompactUnsignedInt(v);
    }

    public void writeInt48(long v) {
        checkNotClosed();
        nativeBytes.writeInt48(v);
    }

    public void writeInt48(long offset, long v) {
        checkNotClosed();
        nativeBytes.writeInt48(offset, v);
    }

    public void writeCompactLong(long v) {
        checkNotClosed();
        nativeBytes.writeCompactLong(v);
    }

    public void writeStopBit(long n) {
        checkNotClosed();
        nativeBytes.writeStopBit(n);
    }

    public void writeCompactDouble(double v) {
        checkNotClosed();
        nativeBytes.writeCompactDouble(v);
    }

    public void write(@NotNull ByteBuffer bb) {
        checkNotClosed();
        nativeBytes.write(bb);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence s, int start, int end) {
        checkNotClosed();
        return nativeBytes.append(s, start, end);
    }

    @NotNull
    public ByteStringAppender append(@Nullable Enum value) {
        checkNotClosed();
        return nativeBytes.append(value);
    }

    @NotNull
    public ByteStringAppender append(boolean b) {
        checkNotClosed();
        return nativeBytes.append(b);
    }

    @NotNull
    public ByteStringAppender append(char c) {
        checkNotClosed();
        return nativeBytes.append(c);
    }

    @NotNull
    public ByteStringAppender append(int num) {
        checkNotClosed();
        return nativeBytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num) {
        checkNotClosed();
        return nativeBytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num, int base) {
        checkNotClosed();
        return nativeBytes.append(num, base);
    }

    @NotNull
    public ByteStringAppender appendDateMillis(long timeInMS) {
        checkNotClosed();
        return nativeBytes.appendDateMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        checkNotClosed();
        return nativeBytes.appendDateTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendTimeMillis(long timeInMS) {
        checkNotClosed();
        return nativeBytes.appendTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender append(double d) {
        checkNotClosed();
        return nativeBytes.append(d);
    }

    public double parseDouble() {
        checkNotClosed();
        return nativeBytes.parseDouble();
    }

    @NotNull
    public <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator) {
        checkNotClosed();
        return nativeBytes.append(list, separator);
    }

    @NotNull
    <E> ByteStringAppender append(@NotNull List<E> list, @NotNull CharSequence separator) {
        checkNotClosed();
        return nativeBytes.append(list, separator);
    }

    @NotNull
    public MutableDecimal parseDecimal(@NotNull MutableDecimal decimal) {
        checkNotClosed();
        return nativeBytes.parseDecimal(decimal);
    }

    public long parseLong() {
        checkNotClosed();
        return nativeBytes.parseLong();
    }

    public long parseLong(int base) {
        checkNotClosed();
        return nativeBytes.parseLong(base);
    }

    @NotNull
    public ByteStringAppender append(double d, int precision) {
        checkNotClosed();
        return nativeBytes.append(d, precision);
    }

    @NotNull
    public ByteStringAppender append(@NotNull MutableDecimal md) {
        checkNotClosed();
        return nativeBytes.append(md);
    }

    @NotNull
    public InputStream inputStream() {
        checkNotClosed();
        return nativeBytes.inputStream();
    }

    @NotNull
    public OutputStream outputStream() {
        checkNotClosed();
        return nativeBytes.outputStream();
    }

    @NotNull
    public ObjectSerializer objectSerializer() {
        checkNotClosed();
        return nativeBytes.objectSerializer();
    }

    public <E> void writeEnum(@Nullable E e) {
        checkNotClosed();
        nativeBytes.writeEnum(e);
    }

    public <E> E readEnum(@NotNull Class<E> eClass) {
        checkNotClosed();
        return nativeBytes.readEnum(eClass);
    }

    public <E extends Enum<E>> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester) {
        checkNotClosed();
        return nativeBytes.parseEnum(eClass, tester);
    }

    public <E> void writeList(@NotNull Collection<E> list) {
        checkNotClosed();
        nativeBytes.writeList(list);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map) {
        checkNotClosed();
        nativeBytes.writeMap(map);
    }

    public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {
        checkNotClosed();
        nativeBytes.readList(list, eClass);
    }

    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        checkNotClosed();
        return nativeBytes.readMap(map, kClass, vClass);
    }

    public int available() {
        checkNotClosed();
        return nativeBytes.available();
    }

    public int read() {
        checkNotClosed();
        return nativeBytes.read();
    }

    public int read(@NotNull byte[] bytes) {
        checkNotClosed();
        return nativeBytes.read(bytes);
    }


    public long skip(long n) {
        checkNotClosed();
        return nativeBytes.skip(n);
    }

    public void close() {
        checkNotClosed();
        nativeBytes.close();
    }

    public void finish() throws IndexOutOfBoundsException {
        nativeBytes.finish();
    }

    public boolean isFinished() {
        checkNotClosed();
        return nativeBytes.isFinished();
    }

    public AbstractBytes clear() {
        checkNotClosed();
        return nativeBytes.clear();
    }

    public Bytes flip() {
        checkNotClosed();
        return nativeBytes.flip();
    }

    public void flush() {
        checkNotClosed();
        nativeBytes.flush();
    }

    @Nullable
    public Object readObject() {
        checkNotClosed();
        return nativeBytes.readObject();
    }

    @Nullable
    public <T> T readObject(Class<T> tClass) throws IllegalStateException {
        return nativeBytes.readObject(tClass);
    }

    @Nullable
    public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
        checkNotClosed();
        return nativeBytes.readInstance(objClass, obj);
    }

    public void writeObject(@Nullable Object obj) {
        checkNotClosed();
        nativeBytes.writeObject(obj);
    }

    public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {
        checkNotClosed();
        nativeBytes.writeInstance(objClass, obj);
    }

    public boolean tryLockInt(long offset) {
        checkNotClosed();
        return nativeBytes.tryLockInt(offset);
    }

    public boolean tryLockNanosInt(long offset, long nanos) {
        checkNotClosed();
        return nativeBytes.tryLockNanosInt(offset, nanos);
    }

    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        nativeBytes.busyLockInt(offset);
    }

    public void unlockInt(long offset) throws IllegalMonitorStateException {
        nativeBytes.unlockInt(offset);
    }

    public void resetLockInt(long offset) {
        checkNotClosed();
        nativeBytes.resetLockInt(offset);
    }

    public int threadIdForLockInt(long offset) {
        checkNotClosed();
        return nativeBytes.threadIdForLockInt(offset);
    }

    int shortThreadId() {
        checkNotClosed();
        return nativeBytes.shortThreadId();
    }

    int shortThreadId0() {
        checkNotClosed();
        return nativeBytes.shortThreadId0();
    }

    Thread currentThread() {
        checkNotClosed();
        return nativeBytes.currentThread();
    }

    public boolean tryLockLong(long offset) {
        checkNotClosed();
        return nativeBytes.tryLockLong(offset);
    }

    long uniqueTid() {
        checkNotClosed();
        return nativeBytes.uniqueTid();
    }

    public boolean tryLockNanosLong(long offset, long nanos) {
        checkNotClosed();
        return nativeBytes.tryLockNanosLong(offset, nanos);
    }

    public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {
        nativeBytes.busyLockLong(offset);
    }

    public void unlockLong(long offset) throws IllegalMonitorStateException {
        nativeBytes.unlockLong(offset);
    }

    public void resetLockLong(long offset) {
        checkNotClosed();
        nativeBytes.resetLockLong(offset);
    }

    public long threadIdForLockLong(long offset) {
        checkNotClosed();
        return nativeBytes.threadIdForLockLong(offset);
    }

    long getId() {
        checkNotClosed();
        return nativeBytes.getId();
    }

    public int getAndAdd(long offset, int delta) {
        checkNotClosed();
        return nativeBytes.getAndAdd(offset, delta);
    }

    public int addAndGetInt(long offset, int delta) {
        checkNotClosed();
        return nativeBytes.addAndGetInt(offset, delta);
    }

    public byte addByte(long offset, byte b) {
        checkNotClosed();
        return nativeBytes.addByte(offset, b);
    }

    public int addUnsignedByte(long offset, int i) {
        checkNotClosed();
        return nativeBytes.addUnsignedByte(offset, i);
    }

    public short addShort(long offset, short s) {
        checkNotClosed();
        return nativeBytes.addShort(offset, s);
    }

    public int addUnsignedShort(long offset, int i) {
        checkNotClosed();
        return nativeBytes.addUnsignedShort(offset, i);
    }

    public int addInt(long offset, int i) {
        checkNotClosed();
        return nativeBytes.addInt(offset, i);
    }

    public long addUnsignedInt(long offset, long i) {
        checkNotClosed();
        return nativeBytes.addUnsignedInt(offset, i);
    }

    public long addLong(long offset, long i) {
        checkNotClosed();
        return nativeBytes.addLong(offset, i);
    }

    public float addFloat(long offset, float f) {
        checkNotClosed();
        return nativeBytes.addFloat(offset, f);
    }

    public double addDouble(long offset, double d) {
        checkNotClosed();
        return nativeBytes.addDouble(offset, d);
    }

    public int addAtomicInt(long offset, int i) {
        checkNotClosed();
        return nativeBytes.addAtomicInt(offset, i);
    }

    public long addAtomicLong(long offset, long delta) {
        checkNotClosed();
        return nativeBytes.addAtomicLong(offset, delta);
    }

    public float addAtomicFloat(long offset, float delta) {
        checkNotClosed();
        return nativeBytes.addAtomicFloat(offset, delta);
    }

    public double addAtomicDouble(long offset, double delta) {
        checkNotClosed();
        return nativeBytes.addAtomicDouble(offset, delta);
    }

    public float readVolatileFloat(long offset) {
        checkNotClosed();
        return nativeBytes.readVolatileFloat(offset);
    }

    public double readVolatileDouble(long offset) {
        checkNotClosed();
        return nativeBytes.readVolatileDouble(offset);
    }

    public void writeOrderedFloat(long offset, float v) {
        checkNotClosed();
        nativeBytes.writeOrderedFloat(offset, v);
    }

    public void writeOrderedDouble(long offset, double v) {
        checkNotClosed();
        nativeBytes.writeOrderedDouble(offset, v);
    }

    public int length() {
        checkNotClosed();
        return nativeBytes.length();
    }

    public char charAt(int index) {
        checkNotClosed();
        return nativeBytes.charAt(index);
    }

    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        nativeBytes.readMarshallable(in);
    }

    public void writeMarshallable(@NotNull Bytes out) {
        checkNotClosed();
        nativeBytes.writeMarshallable(out);
    }

    public void write(RandomDataInput bytes) {
        checkNotClosed();
        nativeBytes.write(bytes);
    }


    public boolean startsWith(RandomDataInput input) {
        checkNotClosed();
        return nativeBytes.startsWith(input);
    }


    @NotNull
    public String toString() {
        checkNotClosed();
        return nativeBytes.toString();
    }

    @NotNull
    public String toDebugString() {
        checkNotClosed();
        return nativeBytes.toDebugString();
    }

    @NotNull
    public String toDebugString(long limit) {
        checkNotClosed();
        return nativeBytes.toDebugString(limit);
    }

    public void toString(Appendable sb, long start, long position, long end) {
        checkNotClosed();
        nativeBytes.toString(sb, start, position, end);
    }


    public void asString(Appendable appendable) {
        checkNotClosed();
        nativeBytes.asString(appendable);
    }

    public CharSequence asString() {
        checkNotClosed();
        return nativeBytes.asString();
    }

    public boolean compareAndSwapDouble(long offset, double expected, double value) {
        checkNotClosed();
        return nativeBytes.compareAndSwapDouble(offset, expected, value);
    }

    public File file() {
        checkNotClosed();
        return nativeBytes.file();
    }

    public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return nativeBytes.tryRWReadLock(offset, timeOutNS);
    }

    public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return nativeBytes.tryRWWriteLock(offset, timeOutNS);
    }

    public void unlockRWReadLock(long offset) {
        checkNotClosed();
        nativeBytes.unlockRWReadLock(offset);
    }

    public void unlockRWWriteLock(long offset) {
        checkNotClosed();
        nativeBytes.unlockRWWriteLock(offset);
    }

    String dumpRWLock(long offset) {
        checkNotClosed();
        return nativeBytes.dumpRWLock(offset);
    }

    public NativeBytes slice() {
        checkNotClosed();
        return nativeBytes.slice();
    }

    public NativeBytes slice(long offset, long length) {
        checkNotClosed();
        return nativeBytes.slice(offset, length);
    }

    public CharSequence subSequence(int start, int end) {
        checkNotClosed();
        return nativeBytes.subSequence(start, end);
    }

    public NativeBytes bytes() {
        checkNotClosed();
        return nativeBytes.bytes();
    }

    public NativeBytes bytes(long offset, long length) {
        checkNotClosed();
        return nativeBytes.bytes(offset, length);
    }

    public long address() {
        checkNotClosed();
        return nativeBytes.address();
    }

    public Bytes zeroOut() {
        checkNotClosed();
        return nativeBytes.zeroOut();
    }

    public Bytes zeroOut(long start, long end) {
        checkNotClosed();
        return nativeBytes.zeroOut(start, end);
    }

    public Bytes zeroOut(long start, long end, boolean ifNotZero) {
        checkNotClosed();
        return nativeBytes.zeroOut(start, end, ifNotZero);
    }

    public int read(@NotNull byte[] bytes, int off, int len) {
        checkNotClosed();
        return nativeBytes.read(bytes, off, len);
    }

    public byte readByte() {
        checkNotClosed();
        return nativeBytes.readByte();
    }

    public byte readByte(long offset) {
        checkNotClosed();
        return nativeBytes.readByte(offset);
    }

    public void readFully(@NotNull byte[] b, int off, int len) {
        checkNotClosed();
        nativeBytes.readFully(b, off, len);
    }

    public void readFully(long offset, byte[] bytes, int off, int len) {
        checkNotClosed();
        nativeBytes.readFully(offset, bytes, off, len);
    }

    public void readFully(@NotNull char[] data, int off, int len) {
        checkNotClosed();
        nativeBytes.readFully(data, off, len);
    }

    public short readShort() {
        checkNotClosed();
        return nativeBytes.readShort();
    }

    public short readShort(long offset) {
        checkNotClosed();
        return nativeBytes.readShort(offset);
    }

    public char readChar() {
        checkNotClosed();
        return nativeBytes.readChar();
    }

    public char readChar(long offset) {
        checkNotClosed();
        return nativeBytes.readChar(offset);
    }

    public int readInt() {
        checkNotClosed();
        return nativeBytes.readInt();
    }

    public int readInt(long offset) {
        checkNotClosed();
        return nativeBytes.readInt(offset);
    }

    public int readVolatileInt() {
        checkNotClosed();
        return nativeBytes.readVolatileInt();
    }

    public int readVolatileInt(long offset) {
        checkNotClosed();
        return nativeBytes.readVolatileInt(offset);
    }

    public long readLong() {
        checkNotClosed();
        return nativeBytes.readLong();
    }

    public long readLong(long offset) {
        checkNotClosed();
        return nativeBytes.readLong(offset);
    }

    public long readVolatileLong() {
        checkNotClosed();
        return nativeBytes.readVolatileLong();
    }

    public long readVolatileLong(long offset) {
        checkNotClosed();
        return nativeBytes.readVolatileLong(offset);
    }

    public float readFloat() {
        checkNotClosed();
        return nativeBytes.readFloat();
    }

    public float readFloat(long offset) {
        checkNotClosed();
        return nativeBytes.readFloat(offset);
    }

    public double readDouble() {
        checkNotClosed();
        return nativeBytes.readDouble();
    }

    public double readDouble(long offset) {
        checkNotClosed();
        return nativeBytes.readDouble(offset);
    }

    public void write(int b) {
        checkNotClosed();
        nativeBytes.write(b);
    }

    public void writeByte(long offset, int b) {
        checkNotClosed();
        nativeBytes.writeByte(offset, b);
    }

    public void write(long offset, @NotNull byte[] bytes) {
        checkNotClosed();
        nativeBytes.write(offset, bytes);
    }

    public void write(byte[] bytes, int off, int len) {
        checkNotClosed();
        nativeBytes.write(bytes, off, len);
    }

    public void write(long offset, byte[] bytes, int off, int len) {
        checkNotClosed();
        nativeBytes.write(offset, bytes, off, len);
    }

    public void writeShort(int v) {
        checkNotClosed();
        nativeBytes.writeShort(v);
    }

    public void writeShort(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeShort(offset, v);
    }

    public void writeChar(int v) {
        checkNotClosed();
        nativeBytes.writeChar(v);
    }

    void addPosition(long delta) {
        checkNotClosed();
        nativeBytes.addPosition(delta);
    }

    public void writeChar(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeChar(offset, v);
    }

    public void writeInt(int v) {
        checkNotClosed();
        nativeBytes.writeInt(v);
    }

    public void writeInt(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeInt(offset, v);
    }

    public void writeOrderedInt(int v) {
        checkNotClosed();
        nativeBytes.writeOrderedInt(v);
    }

    public void writeOrderedInt(long offset, int v) {
        checkNotClosed();
        nativeBytes.writeOrderedInt(offset, v);
    }

    public boolean compareAndSwapInt(long offset, int expected, int x) {
        checkNotClosed();
        return nativeBytes.compareAndSwapInt(offset, expected, x);
    }

    public void writeLong(long v) {
        checkNotClosed();
        nativeBytes.writeLong(v);
    }

    public void writeLong(long offset, long v) {
        checkNotClosed();
        nativeBytes.writeLong(offset, v);
    }

    public void writeOrderedLong(long v) {
        checkNotClosed();
        nativeBytes.writeOrderedLong(v);
    }

    public void writeOrderedLong(long offset, long v) {
        checkNotClosed();
        nativeBytes.writeOrderedLong(offset, v);
    }

    public boolean compareAndSwapLong(long offset, long expected, long x) {
        checkNotClosed();
        return nativeBytes.compareAndSwapLong(offset, expected, x);
    }

    public void writeFloat(float v) {
        checkNotClosed();
        nativeBytes.writeFloat(v);
    }

    public void writeFloat(long offset, float v) {
        checkNotClosed();
        nativeBytes.writeFloat(offset, v);
    }

    public void writeDouble(double v) {
        checkNotClosed();
        nativeBytes.writeDouble(v);
    }

    public void writeDouble(long offset, double v) {
        checkNotClosed();
        nativeBytes.writeDouble(offset, v);
    }

    public void readObject(Object object, int start, int end) {
        checkNotClosed();
        nativeBytes.readObject(object, start, end);
    }

    public void writeObject(Object object, int start, int end) {
        checkNotClosed();
        nativeBytes.writeObject(object, start, end);
    }

    public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
        checkNotClosed();
        return nativeBytes.compare(offset, input, inputOffset, len);
    }

    public long position() {
        checkNotClosed();
        return nativeBytes.position();
    }

    public NativeBytes position(long position) {
        checkNotClosed();
        return nativeBytes.position(position);
    }

    public NativeBytes lazyPosition(long position) {
        checkNotClosed();
        return nativeBytes.lazyPosition(position);
    }

    public void write(RandomDataInput bytes, long position, long length) {
        checkNotClosed();
        nativeBytes.write(bytes, position, length);
    }

    public long capacity() {
        checkNotClosed();
        return nativeBytes.capacity();
    }

    public long remaining() {
        checkNotClosed();
        return nativeBytes.remaining();
    }

    public long limit() {
        checkNotClosed();
        return nativeBytes.limit();
    }

    public NativeBytes limit(long limit) {
        checkNotClosed();
        return nativeBytes.limit(limit);
    }

    @NotNull
    public ByteOrder byteOrder() {
        checkNotClosed();
        return nativeBytes.byteOrder();
    }

    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        nativeBytes.checkEndOfBuffer();
    }

    public long startAddr() {
        checkNotClosed();
        return nativeBytes.startAddr();
    }

    long capacityAddr() {
        checkNotClosed();
        return nativeBytes.capacityAddr();
    }

    protected void cleanup() {
        checkNotClosed();
        nativeBytes.cleanup();
    }

    public Bytes load() {
        checkNotClosed();
        return nativeBytes.load();
    }

    public void alignPositionAddr(int powerOf2) {
        checkNotClosed();
        nativeBytes.alignPositionAddr(powerOf2);
    }

    public void positionAddr(long positionAddr) {
        checkNotClosed();
        nativeBytes.positionAddr(positionAddr);
    }

    void positionChecks(long positionAddr) {
        checkNotClosed();
        nativeBytes.positionChecks(positionAddr);
    }

    boolean actualPositionChecks(long positionAddr) {
        checkNotClosed();
        return nativeBytes.actualPositionChecks(positionAddr);
    }

    void offsetChecks(long offset, long len) {
        checkNotClosed();
        nativeBytes.offsetChecks(offset, len);
    }

    boolean actualOffsetChecks(long offset, long len) {
        checkNotClosed();
        return nativeBytes.actualOffsetChecks(offset, len);
    }

    public long positionAddr() {
        checkNotClosed();
        return nativeBytes.positionAddr();
    }

    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        checkNotClosed();
        return nativeBytes.sliceAsByteBuffer(toReuse);
    }

    protected ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse, Object att) {
        checkNotClosed();
        return nativeBytes.sliceAsByteBuffer(toReuse, att);
    }
}
