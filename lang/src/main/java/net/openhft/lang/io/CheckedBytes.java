package net.openhft.lang.io;

import net.openhft.lang.io.serialization.ObjectSerializer;
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

/**
 * @author Rob Austin.
 */
public class CheckedBytes implements Bytes {
    private final Bytes bytes;

    volatile boolean isClosed = false;


    public CheckedBytes(Bytes bytes) {
        checkNotClosed();
        this.bytes = bytes;
    }


    public void clearThreadAssociation() {
        checkNotClosed();
        bytes.clearThreadAssociation();
    }


    public long size() {
        checkNotClosed();
        return bytes.size();
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
        bytes.free();
    }

    public void reserve() {
        checkNotClosed();
        bytes.reserve();
    }

    public void release() {
        checkNotClosed();
        bytes.release();
    }


    public int refCount() {
        checkNotClosed();
        return bytes.refCount();
    }


    public void selfTerminating(boolean selfTerminating) {
        checkNotClosed();
        bytes.selfTerminating(selfTerminating);
    }

    public boolean selfTerminating() {
        checkNotClosed();
        return bytes.selfTerminating();
    }

    public int readUnsignedByteOrThrow() throws BufferUnderflowException {
        return bytes.readUnsignedByteOrThrow();
    }

    @Override
    public void write(long offset, Bytes bytes) {
        bytes.write(offset, bytes);
    }

    public Boolean parseBoolean(@NotNull StopCharTester tester) {
        checkNotClosed();
        return bytes.parseBoolean(tester);
    }

    public void readFully(@NotNull byte[] bytes) {
        checkNotClosed();
        this.bytes.readFully(bytes);
    }

    public void readFully(@NotNull char[] data) {
        checkNotClosed();
        bytes.readFully(data);
    }

    public int skipBytes(int n) {
        checkNotClosed();
        return bytes.skipBytes(n);
    }

    public boolean readBoolean() {
        checkNotClosed();
        return bytes.readBoolean();
    }

    public boolean readBoolean(long offset) {
        checkNotClosed();
        return bytes.readBoolean(offset);
    }

    public int readUnsignedByte() {
        checkNotClosed();
        return bytes.readUnsignedByte();
    }

    public int readUnsignedByte(long offset) {
        checkNotClosed();
        return bytes.readUnsignedByte(offset);
    }

    public int readUnsignedShort() {
        checkNotClosed();
        return bytes.readUnsignedShort();
    }

    public int readUnsignedShort(long offset) {
        checkNotClosed();
        return bytes.readUnsignedShort(offset);
    }

    @NotNull
    public String readLine() {
        checkNotClosed();
        return bytes.readLine();
    }

    @Nullable
    public String readUTFΔ() {
        checkNotClosed();
        return bytes.readUTFΔ();
    }

    @Nullable
    public String readUTFΔ(long offset) throws IllegalStateException {
        return bytes.readUTFΔ(offset);
    }

    public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
        checkNotClosed();
        return bytes.readUTFΔ(stringBuilder);
    }

    @NotNull
    public String parseUTF(@NotNull StopCharTester tester) {
        checkNotClosed();
        return bytes.parseUTF(tester);
    }

    public void parseUTF(@NotNull StringBuilder builder, @NotNull StopCharTester tester) {
        checkNotClosed();
        bytes.parseUTF(builder, tester);
    }

    public boolean stepBackAndSkipTo(@NotNull StopCharTester tester) {
        checkNotClosed();
        return bytes.stepBackAndSkipTo(tester);
    }

    public boolean skipTo(@NotNull StopCharTester tester) {
        checkNotClosed();
        return bytes.skipTo(tester);
    }

    @NotNull
    public String readUTF() {
        checkNotClosed();
        return bytes.readUTF();
    }

    public short readCompactShort() {
        checkNotClosed();
        return bytes.readCompactShort();
    }

    public int readCompactUnsignedShort() {
        checkNotClosed();
        return bytes.readCompactUnsignedShort();
    }

    public int readInt24() {
        checkNotClosed();
        return bytes.readInt24();
    }

    public int readInt24(long offset) {
        checkNotClosed();
        return bytes.readInt24(offset);
    }

    public long readUnsignedInt() {
        checkNotClosed();
        return bytes.readUnsignedInt();
    }

    public long readUnsignedInt(long offset) {
        checkNotClosed();
        return bytes.readUnsignedInt(offset);
    }

    public int readCompactInt() {
        checkNotClosed();
        return bytes.readCompactInt();
    }

    public long readCompactUnsignedInt() {
        checkNotClosed();
        return bytes.readCompactUnsignedInt();
    }

    public long readInt48() {
        checkNotClosed();
        return bytes.readInt48();
    }

    public long readInt48(long offset) {
        checkNotClosed();
        return bytes.readInt48(offset);
    }

    public long readCompactLong() {
        checkNotClosed();
        return bytes.readCompactLong();
    }

    public long readStopBit() {
        checkNotClosed();
        return bytes.readStopBit();
    }

    public double readCompactDouble() {
        checkNotClosed();
        return bytes.readCompactDouble();
    }

    public void read(@NotNull ByteBuffer bb) {
        checkNotClosed();
        bytes.read(bb);
    }

    public void write(@NotNull byte[] bytes) {
        checkNotClosed();
        this.bytes.write(bytes);
    }

    public void writeBoolean(boolean v) {
        checkNotClosed();
        bytes.writeBoolean(v);
    }

    public void writeBoolean(long offset, boolean v) {
        checkNotClosed();
        bytes.writeBoolean(offset, v);
    }

    public void writeBytes(@NotNull String s) {
        checkNotClosed();
        bytes.writeBytes(s);
    }

    public void writeChars(@NotNull String s) {
        checkNotClosed();
        bytes.writeChars(s);
    }

    public void writeChars(@NotNull CharSequence cs) {
        checkNotClosed();
        bytes.writeChars(cs);
    }

    public void writeUTF(@NotNull String str) {
        checkNotClosed();
        bytes.writeUTF(str);
    }

    public void writeUTFΔ(@Nullable CharSequence str) throws IllegalArgumentException {
        bytes.writeUTFΔ(str);
    }

    public void writeUTFΔ(long offset, int maxSize, @Nullable CharSequence s) throws IllegalStateException {
        bytes.writeUTFΔ(offset, maxSize, s);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence str) {
        checkNotClosed();
        return bytes.append(str);
    }

    public void writeByte(int v) {
        checkNotClosed();
        bytes.writeByte(v);
    }

    public void writeUnsignedByte(int v) {
        checkNotClosed();
        bytes.writeUnsignedByte(v);
    }

    public void writeUnsignedByte(long offset, int v) {
        checkNotClosed();
        bytes.writeUnsignedByte(offset, v);
    }


    public void write(@NotNull char[] data) {
        checkNotClosed();
        bytes.write(data);
    }

    public void write(@NotNull char[] data, int off, int len) {
        checkNotClosed();
        bytes.write(data, off, len);
    }

    public void writeUnsignedShort(int v) {
        checkNotClosed();
        bytes.writeUnsignedShort(v);
    }

    public void writeUnsignedShort(long offset, int v) {
        checkNotClosed();
        bytes.writeUnsignedShort(offset, v);
    }

    public void writeCompactShort(int v) {
        checkNotClosed();
        bytes.writeCompactShort(v);
    }

    public void writeCompactUnsignedShort(int v) {
        checkNotClosed();
        bytes.writeCompactUnsignedShort(v);
    }

    public void writeInt24(int v) {
        checkNotClosed();
        bytes.writeInt24(v);
    }

    public void writeInt24(long offset, int v) {
        checkNotClosed();
        bytes.writeInt24(offset, v);
    }

    public void writeUnsignedInt(long v) {
        checkNotClosed();
        bytes.writeUnsignedInt(v);
    }

    public void writeUnsignedInt(long offset, long v) {
        checkNotClosed();
        bytes.writeUnsignedInt(offset, v);
    }

    public void writeCompactInt(int v) {
        checkNotClosed();
        bytes.writeCompactInt(v);
    }

    public void writeCompactUnsignedInt(long v) {
        checkNotClosed();
        bytes.writeCompactUnsignedInt(v);
    }

    public void writeInt48(long v) {
        checkNotClosed();
        bytes.writeInt48(v);
    }

    public void writeInt48(long offset, long v) {
        checkNotClosed();
        bytes.writeInt48(offset, v);
    }

    public void writeCompactLong(long v) {
        checkNotClosed();
        bytes.writeCompactLong(v);
    }

    public void writeStopBit(long n) {
        checkNotClosed();
        bytes.writeStopBit(n);
    }

    public void writeCompactDouble(double v) {
        checkNotClosed();
        bytes.writeCompactDouble(v);
    }

    public void write(@NotNull ByteBuffer bb) {
        checkNotClosed();
        bytes.write(bb);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence s, int start, int end) {
        checkNotClosed();
        return bytes.append(s, start, end);
    }

    @NotNull
    public ByteStringAppender append(@Nullable Enum value) {
        checkNotClosed();
        return bytes.append(value);
    }

    @NotNull
    public ByteStringAppender append(boolean b) {
        checkNotClosed();
        return bytes.append(b);
    }

    @NotNull
    public ByteStringAppender append(char c) {
        checkNotClosed();
        return bytes.append(c);
    }

    @NotNull
    public ByteStringAppender append(int num) {
        checkNotClosed();
        return bytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num) {
        checkNotClosed();
        return bytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num, int base) {
        checkNotClosed();
        return bytes.append(num, base);
    }

    @NotNull
    public ByteStringAppender appendDateMillis(long timeInMS) {
        checkNotClosed();
        return bytes.appendDateMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        checkNotClosed();
        return bytes.appendDateTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendTimeMillis(long timeInMS) {
        checkNotClosed();
        return bytes.appendTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender append(double d) {
        checkNotClosed();
        return bytes.append(d);
    }

    public double parseDouble() {
        checkNotClosed();
        return bytes.parseDouble();
    }

    @NotNull
    public <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator) {
        checkNotClosed();
        return bytes.append(list, separator);
    }

    @NotNull
    <E> ByteStringAppender append(@NotNull List<E> list, @NotNull CharSequence separator) {
        checkNotClosed();
        return bytes.append(list, separator);
    }

    @NotNull
    public MutableDecimal parseDecimal(@NotNull MutableDecimal decimal) {
        checkNotClosed();
        return bytes.parseDecimal(decimal);
    }

    public long parseLong() {
        checkNotClosed();
        return bytes.parseLong();
    }

    public long parseLong(int base) {
        checkNotClosed();
        return bytes.parseLong(base);
    }

    @NotNull
    public ByteStringAppender append(double d, int precision) {
        checkNotClosed();
        return bytes.append(d, precision);
    }

    @NotNull
    public ByteStringAppender append(@NotNull MutableDecimal md) {
        checkNotClosed();
        return bytes.append(md);
    }

    @NotNull
    public InputStream inputStream() {
        checkNotClosed();
        return bytes.inputStream();
    }

    @NotNull
    public OutputStream outputStream() {
        checkNotClosed();
        return bytes.outputStream();
    }

    @NotNull
    public ObjectSerializer objectSerializer() {
        checkNotClosed();
        return bytes.objectSerializer();
    }

    public <E> void writeEnum(@Nullable E e) {
        checkNotClosed();
        bytes.writeEnum(e);
    }

    public <E> E readEnum(@NotNull Class<E> eClass) {
        checkNotClosed();
        return bytes.readEnum(eClass);
    }

    public <E extends Enum<E>> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester) {
        checkNotClosed();
        return bytes.parseEnum(eClass, tester);
    }

    public <E> void writeList(@NotNull Collection<E> list) {
        checkNotClosed();
        bytes.writeList(list);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map) {
        checkNotClosed();
        bytes.writeMap(map);
    }

    public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {
        checkNotClosed();
        bytes.readList(list, eClass);
    }

    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        checkNotClosed();
        return bytes.readMap(map, kClass, vClass);
    }

    public int available() {
        checkNotClosed();
        return bytes.available();
    }

    public int read() {
        checkNotClosed();
        return bytes.read();
    }

    public int read(@NotNull byte[] bytes) {
        checkNotClosed();
        return this.bytes.read(bytes);
    }


    public long skip(long n) {
        checkNotClosed();
        return bytes.skip(n);
    }

    public void close() {
        checkNotClosed();
        bytes.close();
    }

    public void finish() throws IndexOutOfBoundsException {
        bytes.finish();
    }

    public boolean isFinished() {
        checkNotClosed();
        return bytes.isFinished();
    }

    public Bytes clear() {
        checkNotClosed();
        return bytes.clear();
    }

    public Bytes flip() {
        checkNotClosed();
        return bytes.flip();
    }

    public void flush() {
        checkNotClosed();
        bytes.flush();
    }

    @Nullable
    public Object readObject() {
        checkNotClosed();
        return bytes.readObject();
    }

    @Nullable
    public <T> T readObject(Class<T> tClass) throws IllegalStateException {
        return bytes.readObject(tClass);
    }

    @Nullable
    public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
        checkNotClosed();
        return bytes.readInstance(objClass, obj);
    }

    public void writeObject(@Nullable Object obj) {
        checkNotClosed();
        bytes.writeObject(obj);
    }

    public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {
        checkNotClosed();
        bytes.writeInstance(objClass, obj);
    }

    public boolean tryLockInt(long offset) {
        checkNotClosed();
        return bytes.tryLockInt(offset);
    }

    public boolean tryLockNanosInt(long offset, long nanos) {
        checkNotClosed();
        return bytes.tryLockNanosInt(offset, nanos);
    }

    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        bytes.busyLockInt(offset);
    }

    public void unlockInt(long offset) throws IllegalMonitorStateException {
        bytes.unlockInt(offset);
    }

    public void resetLockInt(long offset) {
        checkNotClosed();
        bytes.resetLockInt(offset);
    }

    public int threadIdForLockInt(long offset) {
        checkNotClosed();
        return bytes.threadIdForLockInt(offset);
    }


    public boolean tryLockLong(long offset) {
        checkNotClosed();
        return bytes.tryLockLong(offset);
    }


    public boolean tryLockNanosLong(long offset, long nanos) {
        checkNotClosed();
        return bytes.tryLockNanosLong(offset, nanos);
    }

    public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {
        bytes.busyLockLong(offset);
    }

    public void unlockLong(long offset) throws IllegalMonitorStateException {
        bytes.unlockLong(offset);
    }

    public void resetLockLong(long offset) {
        checkNotClosed();
        bytes.resetLockLong(offset);
    }

    public long threadIdForLockLong(long offset) {
        checkNotClosed();
        return bytes.threadIdForLockLong(offset);
    }


    public int getAndAdd(long offset, int delta) {
        checkNotClosed();
        return bytes.getAndAdd(offset, delta);
    }

    public int addAndGetInt(long offset, int delta) {
        checkNotClosed();
        return bytes.addAndGetInt(offset, delta);
    }

    public byte addByte(long offset, byte b) {
        checkNotClosed();
        return bytes.addByte(offset, b);
    }

    public int addUnsignedByte(long offset, int i) {
        checkNotClosed();
        return bytes.addUnsignedByte(offset, i);
    }

    public short addShort(long offset, short s) {
        checkNotClosed();
        return bytes.addShort(offset, s);
    }

    public int addUnsignedShort(long offset, int i) {
        checkNotClosed();
        return bytes.addUnsignedShort(offset, i);
    }

    public int addInt(long offset, int i) {
        checkNotClosed();
        return bytes.addInt(offset, i);
    }

    public long addUnsignedInt(long offset, long i) {
        checkNotClosed();
        return bytes.addUnsignedInt(offset, i);
    }

    public long addLong(long offset, long i) {
        checkNotClosed();
        return bytes.addLong(offset, i);
    }

    public float addFloat(long offset, float f) {
        checkNotClosed();
        return bytes.addFloat(offset, f);
    }

    public double addDouble(long offset, double d) {
        checkNotClosed();
        return bytes.addDouble(offset, d);
    }

    public int addAtomicInt(long offset, int i) {
        checkNotClosed();
        return bytes.addAtomicInt(offset, i);
    }

    public long addAtomicLong(long offset, long delta) {
        checkNotClosed();
        return bytes.addAtomicLong(offset, delta);
    }

    public float addAtomicFloat(long offset, float delta) {
        checkNotClosed();
        return bytes.addAtomicFloat(offset, delta);
    }

    public double addAtomicDouble(long offset, double delta) {
        checkNotClosed();
        return bytes.addAtomicDouble(offset, delta);
    }

    public float readVolatileFloat(long offset) {
        checkNotClosed();
        return bytes.readVolatileFloat(offset);
    }

    public double readVolatileDouble(long offset) {
        checkNotClosed();
        return bytes.readVolatileDouble(offset);
    }

    public void writeOrderedFloat(long offset, float v) {
        checkNotClosed();
        bytes.writeOrderedFloat(offset, v);
    }

    public void writeOrderedDouble(long offset, double v) {
        checkNotClosed();
        bytes.writeOrderedDouble(offset, v);
    }

    public int length() {
        checkNotClosed();
        return bytes.length();
    }

    public char charAt(int index) {
        checkNotClosed();
        return bytes.charAt(index);
    }

    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        bytes.readMarshallable(in);
    }

    public void writeMarshallable(@NotNull Bytes out) {
        checkNotClosed();
        bytes.writeMarshallable(out);
    }

    public void write(RandomDataInput bytes) {
        checkNotClosed();
        this.bytes.write(bytes);
    }


    public boolean startsWith(RandomDataInput input) {
        checkNotClosed();
        return bytes.startsWith(input);
    }


    @NotNull
    public String toString() {
        checkNotClosed();
        return bytes.toString();
    }

    @NotNull
    public String toDebugString() {
        checkNotClosed();
        return bytes.toDebugString();
    }

    @NotNull
    public String toDebugString(long limit) {
        checkNotClosed();
        return bytes.toDebugString(limit);
    }

    public void toString(Appendable sb, long start, long position, long end) {
        checkNotClosed();
        bytes.toString(sb, start, position, end);
    }


    public void asString(Appendable appendable) {
        checkNotClosed();
        bytes.asString(appendable);
    }

    public CharSequence asString() {
        checkNotClosed();
        return bytes.asString();
    }

    public boolean compareAndSwapDouble(long offset, double expected, double value) {
        checkNotClosed();
        return bytes.compareAndSwapDouble(offset, expected, value);
    }

    public File file() {
        checkNotClosed();
        return bytes.file();
    }

    public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return bytes.tryRWReadLock(offset, timeOutNS);
    }

    public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return bytes.tryRWWriteLock(offset, timeOutNS);
    }

    public void unlockRWReadLock(long offset) {
        checkNotClosed();
        bytes.unlockRWReadLock(offset);
    }

    public void unlockRWWriteLock(long offset) {
        checkNotClosed();
        bytes.unlockRWWriteLock(offset);
    }


    public Bytes slice() {
        checkNotClosed();
        return bytes.slice();
    }

    public Bytes slice(long offset, long length) {
        checkNotClosed();
        return bytes.slice(offset, length);
    }

    public CharSequence subSequence(int start, int end) {
        checkNotClosed();
        return bytes.subSequence(start, end);
    }

    public Bytes bytes() {
        checkNotClosed();
        return bytes.bytes();
    }

    public Bytes bytes(long offset, long length) {
        checkNotClosed();
        return bytes.bytes(offset, length);
    }

    public long address() {
        checkNotClosed();
        return bytes.address();
    }

    public Bytes zeroOut() {
        checkNotClosed();
        return bytes.zeroOut();
    }

    public Bytes zeroOut(long start, long end) {
        checkNotClosed();
        return bytes.zeroOut(start, end);
    }

    public Bytes zeroOut(long start, long end, boolean ifNotZero) {
        checkNotClosed();
        return bytes.zeroOut(start, end, ifNotZero);
    }

    public int read(@NotNull byte[] bytes, int off, int len) {
        checkNotClosed();
        return this.bytes.read(bytes, off, len);
    }

    public byte readByte() {
        checkNotClosed();
        return bytes.readByte();
    }

    public byte readByte(long offset) {
        checkNotClosed();
        return bytes.readByte(offset);
    }

    public void readFully(@NotNull byte[] b, int off, int len) {
        checkNotClosed();
        bytes.readFully(b, off, len);
    }

    public void readFully(long offset, byte[] bytes, int off, int len) {
        checkNotClosed();
        this.bytes.readFully(offset, bytes, off, len);
    }

    public void readFully(@NotNull char[] data, int off, int len) {
        checkNotClosed();
        bytes.readFully(data, off, len);
    }

    public short readShort() {
        checkNotClosed();
        return bytes.readShort();
    }

    public short readShort(long offset) {
        checkNotClosed();
        return bytes.readShort(offset);
    }

    public char readChar() {
        checkNotClosed();
        return bytes.readChar();
    }

    public char readChar(long offset) {
        checkNotClosed();
        return bytes.readChar(offset);
    }

    public int readInt() {
        checkNotClosed();
        return bytes.readInt();
    }

    public int readInt(long offset) {
        checkNotClosed();
        return bytes.readInt(offset);
    }

    public int readVolatileInt() {
        checkNotClosed();
        return bytes.readVolatileInt();
    }

    public int readVolatileInt(long offset) {
        checkNotClosed();
        return bytes.readVolatileInt(offset);
    }

    public long readLong() {
        checkNotClosed();
        return bytes.readLong();
    }

    public long readLong(long offset) {
        checkNotClosed();
        return bytes.readLong(offset);
    }

    public long readVolatileLong() {
        checkNotClosed();
        return bytes.readVolatileLong();
    }

    public long readVolatileLong(long offset) {
        checkNotClosed();
        return bytes.readVolatileLong(offset);
    }

    public float readFloat() {
        checkNotClosed();
        return bytes.readFloat();
    }

    public float readFloat(long offset) {
        checkNotClosed();
        return bytes.readFloat(offset);
    }

    public double readDouble() {
        checkNotClosed();
        return bytes.readDouble();
    }

    public double readDouble(long offset) {
        checkNotClosed();
        return bytes.readDouble(offset);
    }

    public void write(int b) {
        checkNotClosed();
        bytes.write(b);
    }

    public void writeByte(long offset, int b) {
        checkNotClosed();
        bytes.writeByte(offset, b);
    }

    public void write(long offset, @NotNull byte[] bytes) {
        checkNotClosed();
        this.bytes.write(offset, bytes);
    }

    public void write(byte[] bytes, int off, int len) {
        checkNotClosed();
        this.bytes.write(bytes, off, len);
    }

    public void write(long offset, byte[] bytes, int off, int len) {
        checkNotClosed();
        this.bytes.write(offset, bytes, off, len);
    }

    public void writeShort(int v) {
        checkNotClosed();
        bytes.writeShort(v);
    }

    public void writeShort(long offset, int v) {
        checkNotClosed();
        bytes.writeShort(offset, v);
    }

    public void writeChar(int v) {
        checkNotClosed();
        bytes.writeChar(v);
    }


    public void writeChar(long offset, int v) {
        checkNotClosed();
        bytes.writeChar(offset, v);
    }

    public void writeInt(int v) {
        checkNotClosed();
        bytes.writeInt(v);
    }

    public void writeInt(long offset, int v) {
        checkNotClosed();
        bytes.writeInt(offset, v);
    }

    public void writeOrderedInt(int v) {
        checkNotClosed();
        bytes.writeOrderedInt(v);
    }

    public void writeOrderedInt(long offset, int v) {
        checkNotClosed();
        bytes.writeOrderedInt(offset, v);
    }

    public boolean compareAndSwapInt(long offset, int expected, int x) {
        checkNotClosed();
        return bytes.compareAndSwapInt(offset, expected, x);
    }

    public void writeLong(long v) {
        checkNotClosed();
        bytes.writeLong(v);
    }

    public void writeLong(long offset, long v) {
        checkNotClosed();
        bytes.writeLong(offset, v);
    }

    public void writeOrderedLong(long v) {
        checkNotClosed();
        bytes.writeOrderedLong(v);
    }

    public void writeOrderedLong(long offset, long v) {
        checkNotClosed();
        bytes.writeOrderedLong(offset, v);
    }

    public boolean compareAndSwapLong(long offset, long expected, long x) {
        checkNotClosed();
        return bytes.compareAndSwapLong(offset, expected, x);
    }

    public void writeFloat(float v) {
        checkNotClosed();
        bytes.writeFloat(v);
    }

    public void writeFloat(long offset, float v) {
        checkNotClosed();
        bytes.writeFloat(offset, v);
    }

    public void writeDouble(double v) {
        checkNotClosed();
        bytes.writeDouble(v);
    }

    public void writeDouble(long offset, double v) {
        checkNotClosed();
        bytes.writeDouble(offset, v);
    }

    public void readObject(Object object, int start, int end) {
        checkNotClosed();
        bytes.readObject(object, start, end);
    }

    public void writeObject(Object object, int start, int end) {
        checkNotClosed();
        bytes.writeObject(object, start, end);
    }

    public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
        checkNotClosed();
        return bytes.compare(offset, input, inputOffset, len);
    }

    public long position() {
        checkNotClosed();
        return bytes.position();
    }

    public Bytes position(long position) {
        checkNotClosed();
        return bytes.position(position);
    }


    public void write(RandomDataInput bytes, long position, long length) {
        checkNotClosed();
        this.bytes.write(bytes, position, length);
    }

    public long capacity() {
        checkNotClosed();
        return bytes.capacity();
    }

    public long remaining() {
        checkNotClosed();
        return bytes.remaining();
    }

    public long limit() {
        checkNotClosed();
        return bytes.limit();
    }

    public Bytes limit(long limit) {
        checkNotClosed();
        return bytes.limit(limit);
    }

    @NotNull
    public ByteOrder byteOrder() {
        checkNotClosed();
        return bytes.byteOrder();
    }

    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        bytes.checkEndOfBuffer();
    }


    public Bytes load() {
        checkNotClosed();
        return bytes.load();
    }

    public void alignPositionAddr(int powerOf2) {
        checkNotClosed();
        bytes.alignPositionAddr(powerOf2);
    }


    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        checkNotClosed();
        return bytes.sliceAsByteBuffer(toReuse);
    }


}
