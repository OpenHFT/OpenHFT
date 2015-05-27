//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.openhft.lang.io;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.openhft.lang.io.ByteStringAppender;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.MutableDecimal;
import net.openhft.lang.io.RandomDataInput;
import net.openhft.lang.io.StopCharTester;
import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.model.Byteable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckedBytes implements Bytes {
    private final Bytes bytes;
    volatile boolean isClosed = false;

    public CheckedBytes(Bytes bytes) {
        this.checkNotClosed();
        this.bytes = bytes;
    }

    public void clearThreadAssociation() {
        this.checkNotClosed();
        this.bytes.clearThreadAssociation();
    }

    public long size() {
        this.checkNotClosed();
        return this.bytes.size();
    }

    void checkNotClosed() {
        if(this.isClosed) {
            System.err.print("Thread " + Thread.currentThread().getName() + " performing processing " + "after free()");
            ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
            ThreadInfo[] arr$ = threads;
            int len$ = threads.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                ThreadInfo info = arr$[i$];
                System.out.print(info);
            }

            System.exit(-1);
        }

    }

    public void free() {
        this.isClosed = true;
        this.bytes.free();
    }

    public void reserve() {
        this.checkNotClosed();
        this.bytes.reserve();
    }

    public boolean release() {
        this.checkNotClosed();
        return this.bytes.release();
    }

    public int refCount() {
        this.checkNotClosed();
        return this.bytes.refCount();
    }

    public void selfTerminating(boolean selfTerminating) {
        this.checkNotClosed();
        this.bytes.selfTerminating(selfTerminating);
    }

    public boolean selfTerminating() {
        this.checkNotClosed();
        return this.bytes.selfTerminating();
    }

    public int readUnsignedByteOrThrow() throws BufferUnderflowException {
        return this.bytes.readUnsignedByteOrThrow();
    }

    public void write(long offset, Bytes bytes) {
        bytes.write(offset, bytes);
    }

    public Boolean parseBoolean(@NotNull StopCharTester tester) {
        this.checkNotClosed();
        return this.bytes.parseBoolean(tester);
    }

    public void readFully(@NotNull byte[] bytes) {
        this.checkNotClosed();
        this.bytes.readFully(bytes);
    }

    public void readFully(@NotNull char[] data) {
        this.checkNotClosed();
        this.bytes.readFully(data);
    }

    public int skipBytes(int n) {
        this.checkNotClosed();
        return this.bytes.skipBytes(n);
    }

    public boolean readBoolean() {
        this.checkNotClosed();
        return this.bytes.readBoolean();
    }

    public boolean readBoolean(long offset) {
        this.checkNotClosed();
        return this.bytes.readBoolean(offset);
    }

    public int readUnsignedByte() {
        this.checkNotClosed();
        return this.bytes.readUnsignedByte();
    }

    public int readUnsignedByte(long offset) {
        this.checkNotClosed();
        return this.bytes.readUnsignedByte(offset);
    }

    public int readUnsignedShort() {
        this.checkNotClosed();
        return this.bytes.readUnsignedShort();
    }

    public int readUnsignedShort(long offset) {
        this.checkNotClosed();
        return this.bytes.readUnsignedShort(offset);
    }

    @NotNull
    public String readLine() {
        this.checkNotClosed();
        return this.bytes.readLine();
    }

    @Nullable
    public String readUTFΔ() {
        this.checkNotClosed();
        return this.bytes.readUTFΔ();
    }

    @Nullable
    public String readUTFΔ(long offset) throws IllegalStateException {
        return this.bytes.readUTFΔ(offset);
    }

    public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
        this.checkNotClosed();
        return this.bytes.readUTFΔ(stringBuilder);
    }

    @Override
    public boolean read8bitText(@NotNull StringBuilder stringBuilder) throws StreamCorruptedException {
        return false;
    }

    @NotNull
    public String parseUTF(@NotNull StopCharTester tester) {
        this.checkNotClosed();
        return this.bytes.parseUTF(tester);
    }

    public void parseUTF(@NotNull StringBuilder builder, @NotNull StopCharTester tester) {
        this.checkNotClosed();
        this.bytes.parseUTF(builder, tester);
    }

    public boolean stepBackAndSkipTo(@NotNull StopCharTester tester) {
        this.checkNotClosed();
        return this.bytes.stepBackAndSkipTo(tester);
    }

    public boolean skipTo(@NotNull StopCharTester tester) {
        this.checkNotClosed();
        return this.bytes.skipTo(tester);
    }

    @NotNull
    public String readUTF() {
        this.checkNotClosed();
        return this.bytes.readUTF();
    }

    public short readCompactShort() {
        this.checkNotClosed();
        return this.bytes.readCompactShort();
    }

    public int readCompactUnsignedShort() {
        this.checkNotClosed();
        return this.bytes.readCompactUnsignedShort();
    }

    public int readInt24() {
        this.checkNotClosed();
        return this.bytes.readInt24();
    }

    public int readInt24(long offset) {
        this.checkNotClosed();
        return this.bytes.readInt24(offset);
    }

    public long readUnsignedInt() {
        this.checkNotClosed();
        return this.bytes.readUnsignedInt();
    }

    public long readUnsignedInt(long offset) {
        this.checkNotClosed();
        return this.bytes.readUnsignedInt(offset);
    }

    public int readCompactInt() {
        this.checkNotClosed();
        return this.bytes.readCompactInt();
    }

    public long readCompactUnsignedInt() {
        this.checkNotClosed();
        return this.bytes.readCompactUnsignedInt();
    }

    public long readInt48() {
        this.checkNotClosed();
        return this.bytes.readInt48();
    }

    public long readInt48(long offset) {
        this.checkNotClosed();
        return this.bytes.readInt48(offset);
    }

    public long readCompactLong() {
        this.checkNotClosed();
        return this.bytes.readCompactLong();
    }

    public long readStopBit() {
        this.checkNotClosed();
        return this.bytes.readStopBit();
    }

    public double readCompactDouble() {
        this.checkNotClosed();
        return this.bytes.readCompactDouble();
    }

    public void read(@NotNull ByteBuffer bb) {
        this.checkNotClosed();
        this.bytes.read(bb);
    }

    public void read(@NotNull ByteBuffer bb, int length) {
        this.checkNotClosed();
        this.bytes.read(bb, length);
    }

    public void write(@NotNull byte[] bytes) {
        this.checkNotClosed();
        this.bytes.write(bytes);
    }

    public void writeBoolean(boolean v) {
        this.checkNotClosed();
        this.bytes.writeBoolean(v);
    }

    public void writeBoolean(long offset, boolean v) {
        this.checkNotClosed();
        this.bytes.writeBoolean(offset, v);
    }

    public void writeBytes(@NotNull String s) {
        this.checkNotClosed();
        this.bytes.writeBytes(s);
    }

    public void writeChars(@NotNull String s) {
        this.checkNotClosed();
        this.bytes.writeChars(s);
    }

    public void writeChars(@NotNull CharSequence cs) {
        this.checkNotClosed();
        this.bytes.writeChars(cs);
    }

    public void writeUTF(@NotNull String str) {
        this.checkNotClosed();
        this.bytes.writeUTF(str);
    }

    public void writeUTFΔ(@Nullable CharSequence str) throws IllegalArgumentException {
        this.bytes.writeUTFΔ(str);
    }

    public void writeUTFΔ(long offset, int maxSize, @Nullable CharSequence s) throws IllegalStateException {
        this.bytes.writeUTFΔ(offset, maxSize, s);
    }

    @Override
    public void write8bitText(@Nullable CharSequence s) {

    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence str) {
        this.checkNotClosed();
        return this.bytes.append(str);
    }

    public void writeByte(int v) {
        this.checkNotClosed();
        this.bytes.writeByte(v);
    }

    public void writeUnsignedByte(int v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedByte(v);
    }

    public void writeUnsignedByte(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedByte(offset, v);
    }

    public void write(@NotNull char[] data) {
        this.checkNotClosed();
        this.bytes.write(data);
    }

    public void write(@NotNull char[] data, int off, int len) {
        this.checkNotClosed();
        this.bytes.write(data, off, len);
    }

    public void writeUnsignedShort(int v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedShort(v);
    }

    public void writeUnsignedShort(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedShort(offset, v);
    }

    public void writeCompactShort(int v) {
        this.checkNotClosed();
        this.bytes.writeCompactShort(v);
    }

    public void writeCompactUnsignedShort(int v) {
        this.checkNotClosed();
        this.bytes.writeCompactUnsignedShort(v);
    }

    public void writeInt24(int v) {
        this.checkNotClosed();
        this.bytes.writeInt24(v);
    }

    public void writeInt24(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeInt24(offset, v);
    }

    public void writeUnsignedInt(long v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedInt(v);
    }

    public void writeUnsignedInt(long offset, long v) {
        this.checkNotClosed();
        this.bytes.writeUnsignedInt(offset, v);
    }

    public void writeCompactInt(int v) {
        this.checkNotClosed();
        this.bytes.writeCompactInt(v);
    }

    public void writeCompactUnsignedInt(long v) {
        this.checkNotClosed();
        this.bytes.writeCompactUnsignedInt(v);
    }

    public void writeInt48(long v) {
        this.checkNotClosed();
        this.bytes.writeInt48(v);
    }

    public void writeInt48(long offset, long v) {
        this.checkNotClosed();
        this.bytes.writeInt48(offset, v);
    }

    public void writeCompactLong(long v) {
        this.checkNotClosed();
        this.bytes.writeCompactLong(v);
    }

    public void writeStopBit(long n) {
        this.checkNotClosed();
        this.bytes.writeStopBit(n);
    }

    public void writeCompactDouble(double v) {
        this.checkNotClosed();
        this.bytes.writeCompactDouble(v);
    }

    public void write(@NotNull ByteBuffer bb) {
        this.checkNotClosed();
        this.bytes.write(bb);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence s, int start, int end) {
        this.checkNotClosed();
        return this.bytes.append(s, start, end);
    }

    @NotNull
    public ByteStringAppender append(@Nullable Enum value) {
        this.checkNotClosed();
        return this.bytes.append(value);
    }

    @NotNull
    public ByteStringAppender append(boolean b) {
        this.checkNotClosed();
        return this.bytes.append(b);
    }

    @NotNull
    public ByteStringAppender append(char c) {
        this.checkNotClosed();
        return this.bytes.append(c);
    }

    @NotNull
    public ByteStringAppender append(int num) {
        this.checkNotClosed();
        return this.bytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num) {
        this.checkNotClosed();
        return this.bytes.append(num);
    }

    @NotNull
    public ByteStringAppender append(long num, int base) {
        this.checkNotClosed();
        return this.bytes.append(num, base);
    }

    @NotNull
    public ByteStringAppender appendDateMillis(long timeInMS) {
        this.checkNotClosed();
        return this.bytes.appendDateMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        this.checkNotClosed();
        return this.bytes.appendDateTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender appendTimeMillis(long timeInMS) {
        this.checkNotClosed();
        return this.bytes.appendTimeMillis(timeInMS);
    }

    @NotNull
    public ByteStringAppender append(double d) {
        this.checkNotClosed();
        return this.bytes.append(d);
    }

    public double parseDouble() {
        this.checkNotClosed();
        return this.bytes.parseDouble();
    }

    @NotNull
    public <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator) {
        this.checkNotClosed();
        return this.bytes.append(list, separator);
    }

    @NotNull
    <E> ByteStringAppender append(@NotNull List<E> list, @NotNull CharSequence separator) {
        this.checkNotClosed();
        return this.bytes.append(list, separator);
    }

    @NotNull
    public MutableDecimal parseDecimal(@NotNull MutableDecimal decimal) {
        this.checkNotClosed();
        return this.bytes.parseDecimal(decimal);
    }

    public long parseLong() {
        this.checkNotClosed();
        return this.bytes.parseLong();
    }

    public long parseLong(int base) {
        this.checkNotClosed();
        return this.bytes.parseLong(base);
    }

    @NotNull
    public ByteStringAppender append(double d, int precision) {
        this.checkNotClosed();
        return this.bytes.append(d, precision);
    }

    @NotNull
    public ByteStringAppender append(@NotNull MutableDecimal md) {
        this.checkNotClosed();
        return this.bytes.append(md);
    }

    @NotNull
    public InputStream inputStream() {
        this.checkNotClosed();
        return this.bytes.inputStream();
    }

    @NotNull
    public OutputStream outputStream() {
        this.checkNotClosed();
        return this.bytes.outputStream();
    }

    @NotNull
    public ObjectSerializer objectSerializer() {
        this.checkNotClosed();
        return this.bytes.objectSerializer();
    }

    public <E> void writeEnum(@Nullable E e) {
        this.checkNotClosed();
        this.bytes.writeEnum(e);
    }

    public <E> E readEnum(@NotNull Class<E> eClass) {
        this.checkNotClosed();
        return this.bytes.readEnum(eClass);
    }

    public <E extends Enum<E>> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester) {
        this.checkNotClosed();
        return this.bytes.parseEnum(eClass, tester);
    }

    public <E> void writeList(@NotNull Collection<E> list) {
        this.checkNotClosed();
        this.bytes.writeList(list);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map) {
        this.checkNotClosed();
        this.bytes.writeMap(map);
    }

    public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {
        this.checkNotClosed();
        this.bytes.readList(list, eClass);
    }

    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        this.checkNotClosed();
        return this.bytes.readMap(map, kClass, vClass);
    }

    public int available() {
        this.checkNotClosed();
        return this.bytes.available();
    }

    public int read() {
        this.checkNotClosed();
        return this.bytes.read();
    }

    public int read(@NotNull byte[] bytes) {
        this.checkNotClosed();
        return this.bytes.read(bytes);
    }

    public long skip(long n) {
        this.checkNotClosed();
        return this.bytes.skip(n);
    }

    public void close() {
        this.checkNotClosed();
        this.bytes.close();
    }

    public void finish() throws IndexOutOfBoundsException {
        this.bytes.finish();
    }

    public boolean isFinished() {
        this.checkNotClosed();
        return this.bytes.isFinished();
    }

    public Bytes clear() {
        this.checkNotClosed();
        return this.bytes.clear();
    }

    public Bytes flip() {
        this.checkNotClosed();
        return this.bytes.flip();
    }

    public void flush() {
        this.checkNotClosed();
        this.bytes.flush();
    }

    @Nullable
    public Object readObject() {
        this.checkNotClosed();
        return this.bytes.readObject();
    }

    @Nullable
    public <T> T readObject(Class<T> tClass) throws IllegalStateException {
        return this.bytes.readObject(tClass);
    }

    @Nullable
    public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
        this.checkNotClosed();
        return this.bytes.readInstance(objClass, obj);
    }

    public void writeObject(@Nullable Object obj) {
        this.checkNotClosed();
        this.bytes.writeObject(obj);
    }

    public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {
        this.checkNotClosed();
        this.bytes.writeInstance(objClass, obj);
    }

    public boolean tryLockInt(long offset) {
        this.checkNotClosed();
        return this.bytes.tryLockInt(offset);
    }

    public boolean tryLockNanosInt(long offset, long nanos) {
        this.checkNotClosed();
        return this.bytes.tryLockNanosInt(offset, nanos);
    }

    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        this.bytes.busyLockInt(offset);
    }

    public void unlockInt(long offset) throws IllegalMonitorStateException {
        this.bytes.unlockInt(offset);
    }

    public void resetLockInt(long offset) {
        this.checkNotClosed();
        this.bytes.resetLockInt(offset);
    }

    public int threadIdForLockInt(long offset) {
        this.checkNotClosed();
        return this.bytes.threadIdForLockInt(offset);
    }

    public boolean tryLockLong(long offset) {
        this.checkNotClosed();
        return this.bytes.tryLockLong(offset);
    }

    public boolean tryLockNanosLong(long offset, long nanos) {
        this.checkNotClosed();
        return this.bytes.tryLockNanosLong(offset, nanos);
    }

    public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {
        this.bytes.busyLockLong(offset);
    }

    public void unlockLong(long offset) throws IllegalMonitorStateException {
        this.bytes.unlockLong(offset);
    }

    public void resetLockLong(long offset) {
        this.checkNotClosed();
        this.bytes.resetLockLong(offset);
    }

    public long threadIdForLockLong(long offset) {
        this.checkNotClosed();
        return this.bytes.threadIdForLockLong(offset);
    }

    public int getAndAdd(long offset, int delta) {
        this.checkNotClosed();
        return this.bytes.getAndAdd(offset, delta);
    }

    public int addAndGetInt(long offset, int delta) {
        this.checkNotClosed();
        return this.bytes.addAndGetInt(offset, delta);
    }

    public byte addByte(long offset, byte b) {
        this.checkNotClosed();
        return this.bytes.addByte(offset, b);
    }

    public int addUnsignedByte(long offset, int i) {
        this.checkNotClosed();
        return this.bytes.addUnsignedByte(offset, i);
    }

    public short addShort(long offset, short s) {
        this.checkNotClosed();
        return this.bytes.addShort(offset, s);
    }

    public int addUnsignedShort(long offset, int i) {
        this.checkNotClosed();
        return this.bytes.addUnsignedShort(offset, i);
    }

    public int addInt(long offset, int i) {
        this.checkNotClosed();
        return this.bytes.addInt(offset, i);
    }

    public long addUnsignedInt(long offset, long i) {
        this.checkNotClosed();
        return this.bytes.addUnsignedInt(offset, i);
    }

    public long addLong(long offset, long i) {
        this.checkNotClosed();
        return this.bytes.addLong(offset, i);
    }

    public float addFloat(long offset, float f) {
        this.checkNotClosed();
        return this.bytes.addFloat(offset, f);
    }

    public double addDouble(long offset, double d) {
        this.checkNotClosed();
        return this.bytes.addDouble(offset, d);
    }

    public int addAtomicInt(long offset, int i) {
        this.checkNotClosed();
        return this.bytes.addAtomicInt(offset, i);
    }

    public long addAtomicLong(long offset, long delta) {
        this.checkNotClosed();
        return this.bytes.addAtomicLong(offset, delta);
    }

    public float addAtomicFloat(long offset, float delta) {
        this.checkNotClosed();
        return this.bytes.addAtomicFloat(offset, delta);
    }

    public double addAtomicDouble(long offset, double delta) {
        this.checkNotClosed();
        return this.bytes.addAtomicDouble(offset, delta);
    }

    public float readVolatileFloat(long offset) {
        this.checkNotClosed();
        return this.bytes.readVolatileFloat(offset);
    }

    public double readVolatileDouble(long offset) {
        this.checkNotClosed();
        return this.bytes.readVolatileDouble(offset);
    }

    public void writeOrderedFloat(long offset, float v) {
        this.checkNotClosed();
        this.bytes.writeOrderedFloat(offset, v);
    }

    public void writeOrderedDouble(long offset, double v) {
        this.checkNotClosed();
        this.bytes.writeOrderedDouble(offset, v);
    }

    public int length() {
        this.checkNotClosed();
        return this.bytes.length();
    }

    public char charAt(int index) {
        this.checkNotClosed();
        return this.bytes.charAt(index);
    }

    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        this.bytes.readMarshallable(in);
    }

    public void writeMarshallable(@NotNull Bytes out) {
        this.checkNotClosed();
        this.bytes.writeMarshallable(out);
    }

    public void write(RandomDataInput bytes) {
        this.checkNotClosed();
        this.bytes.write(bytes);
    }

    public void write(Byteable byteable) {
        this.checkNotClosed();
        this.bytes.write(byteable);
    }

    public boolean startsWith(RandomDataInput input) {
        this.checkNotClosed();
        return this.bytes.startsWith(input);
    }

    @NotNull
    public String toString() {
        this.checkNotClosed();
        return this.bytes.toString();
    }

    @NotNull
    public String toDebugString() {
        this.checkNotClosed();
        return this.bytes.toDebugString();
    }

    @NotNull
    public String toDebugString(long limit) {
        this.checkNotClosed();
        return this.bytes.toDebugString(limit);
    }

    public void toString(Appendable sb, long start, long position, long end) {
        this.checkNotClosed();
        this.bytes.toString(sb, start, position, end);
    }

    public void asString(Appendable appendable) {
        this.checkNotClosed();
        this.bytes.asString(appendable);
    }

    public CharSequence asString() {
        this.checkNotClosed();
        return this.bytes.asString();
    }

    public boolean compareAndSwapDouble(long offset, double expected, double value) {
        this.checkNotClosed();
        return this.bytes.compareAndSwapDouble(offset, expected, value);
    }

    public File file() {
        this.checkNotClosed();
        return this.bytes.file();
    }

    public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return this.bytes.tryRWReadLock(offset, timeOutNS);
    }

    public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return this.bytes.tryRWWriteLock(offset, timeOutNS);
    }

    public void unlockRWReadLock(long offset) {
        this.checkNotClosed();
        this.bytes.unlockRWReadLock(offset);
    }

    public void unlockRWWriteLock(long offset) {
        this.checkNotClosed();
        this.bytes.unlockRWWriteLock(offset);
    }

    public Bytes slice() {
        this.checkNotClosed();
        return this.bytes.slice();
    }

    public Bytes slice(long offset, long length) {
        this.checkNotClosed();
        return this.bytes.slice(offset, length);
    }

    public CharSequence subSequence(int start, int end) {
        this.checkNotClosed();
        return this.bytes.subSequence(start, end);
    }

    public Bytes bytes() {
        this.checkNotClosed();
        return this.bytes.bytes();
    }

    public Bytes bytes(long offset, long length) {
        this.checkNotClosed();
        return this.bytes.bytes(offset, length);
    }

    public long address() {
        this.checkNotClosed();
        return this.bytes.address();
    }

    public Bytes zeroOut() {
        this.checkNotClosed();
        return this.bytes.zeroOut();
    }

    public Bytes zeroOut(long start, long end) {
        this.checkNotClosed();
        return this.bytes.zeroOut(start, end);
    }

    public Bytes zeroOut(long start, long end, boolean ifNotZero) {
        this.checkNotClosed();
        return this.bytes.zeroOut(start, end, ifNotZero);
    }

    public int read(@NotNull byte[] bytes, int off, int len) {
        this.checkNotClosed();
        return this.bytes.read(bytes, off, len);
    }

    public byte readByte() {
        this.checkNotClosed();
        return this.bytes.readByte();
    }

    public byte readByte(long offset) {
        this.checkNotClosed();
        return this.bytes.readByte(offset);
    }

    public void readFully(@NotNull byte[] b, int off, int len) {
        this.checkNotClosed();
        this.bytes.readFully(b, off, len);
    }

    public void readFully(long offset, byte[] bytes, int off, int len) {
        this.checkNotClosed();
        this.bytes.readFully(offset, bytes, off, len);
    }

    public void readFully(@NotNull char[] data, int off, int len) {
        this.checkNotClosed();
        this.bytes.readFully(data, off, len);
    }

    public short readShort() {
        this.checkNotClosed();
        return this.bytes.readShort();
    }

    public short readShort(long offset) {
        this.checkNotClosed();
        return this.bytes.readShort(offset);
    }

    public char readChar() {
        this.checkNotClosed();
        return this.bytes.readChar();
    }

    public char readChar(long offset) {
        this.checkNotClosed();
        return this.bytes.readChar(offset);
    }

    public int readInt() {
        this.checkNotClosed();
        return this.bytes.readInt();
    }

    public int readInt(long offset) {
        this.checkNotClosed();
        return this.bytes.readInt(offset);
    }

    public int readVolatileInt() {
        this.checkNotClosed();
        return this.bytes.readVolatileInt();
    }

    public int readVolatileInt(long offset) {
        this.checkNotClosed();
        return this.bytes.readVolatileInt(offset);
    }

    public long readLong() {
        this.checkNotClosed();
        return this.bytes.readLong();
    }

    public long readLong(long offset) {
        this.checkNotClosed();
        return this.bytes.readLong(offset);
    }

    public long readVolatileLong() {
        this.checkNotClosed();
        return this.bytes.readVolatileLong();
    }

    public long readVolatileLong(long offset) {
        this.checkNotClosed();
        return this.bytes.readVolatileLong(offset);
    }

    public float readFloat() {
        this.checkNotClosed();
        return this.bytes.readFloat();
    }

    public float readFloat(long offset) {
        this.checkNotClosed();
        return this.bytes.readFloat(offset);
    }

    public double readDouble() {
        this.checkNotClosed();
        return this.bytes.readDouble();
    }

    public double readDouble(long offset) {
        this.checkNotClosed();
        return this.bytes.readDouble(offset);
    }

    public void write(int b) {
        this.checkNotClosed();
        this.bytes.write(b);
    }

    public void writeByte(long offset, int b) {
        this.checkNotClosed();
        this.bytes.writeByte(offset, b);
    }

    public void write(long offset, @NotNull byte[] bytes) {
        this.checkNotClosed();
        this.bytes.write(offset, bytes);
    }

    public void write(byte[] bytes, int off, int len) {
        this.checkNotClosed();
        this.bytes.write(bytes, off, len);
    }

    public void write(long offset, byte[] bytes, int off, int len) {
        this.checkNotClosed();
        this.bytes.write(offset, bytes, off, len);
    }

    public void writeShort(int v) {
        this.checkNotClosed();
        this.bytes.writeShort(v);
    }

    public void writeShort(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeShort(offset, v);
    }

    public void writeChar(int v) {
        this.checkNotClosed();
        this.bytes.writeChar(v);
    }

    public void writeChar(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeChar(offset, v);
    }

    public void writeInt(int v) {
        this.checkNotClosed();
        this.bytes.writeInt(v);
    }

    public void writeInt(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeInt(offset, v);
    }

    public void writeOrderedInt(int v) {
        this.checkNotClosed();
        this.bytes.writeOrderedInt(v);
    }

    public void writeOrderedInt(long offset, int v) {
        this.checkNotClosed();
        this.bytes.writeOrderedInt(offset, v);
    }

    public boolean compareAndSwapInt(long offset, int expected, int x) {
        this.checkNotClosed();
        return this.bytes.compareAndSwapInt(offset, expected, x);
    }

    public void writeLong(long v) {
        this.checkNotClosed();
        this.bytes.writeLong(v);
    }

    public void writeLong(long offset, long v) {
        this.checkNotClosed();
        this.bytes.writeLong(offset, v);
    }

    public void writeOrderedLong(long v) {
        this.checkNotClosed();
        this.bytes.writeOrderedLong(v);
    }

    public void writeOrderedLong(long offset, long v) {
        this.checkNotClosed();
        this.bytes.writeOrderedLong(offset, v);
    }

    public boolean compareAndSwapLong(long offset, long expected, long x) {
        this.checkNotClosed();
        return this.bytes.compareAndSwapLong(offset, expected, x);
    }

    public void writeFloat(float v) {
        this.checkNotClosed();
        this.bytes.writeFloat(v);
    }

    public void writeFloat(long offset, float v) {
        this.checkNotClosed();
        this.bytes.writeFloat(offset, v);
    }

    public void writeDouble(double v) {
        this.checkNotClosed();
        this.bytes.writeDouble(v);
    }

    public void writeDouble(long offset, double v) {
        this.checkNotClosed();
        this.bytes.writeDouble(offset, v);
    }

    public void readObject(Object object, int start, int end) {
        this.checkNotClosed();
        this.bytes.readObject(object, start, end);
    }

    public void writeObject(Object object, int start, int end) {
        this.checkNotClosed();
        this.bytes.writeObject(object, start, end);
    }

    public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
        this.checkNotClosed();
        return this.bytes.compare(offset, input, inputOffset, len);
    }

    public long position() {
        this.checkNotClosed();
        return this.bytes.position();
    }

    public Bytes position(long position) {
        this.checkNotClosed();
        return this.bytes.position(position);
    }

    public void write(RandomDataInput bytes, long position, long length) {
        this.checkNotClosed();
        this.bytes.write(bytes, position, length);
    }

    public long capacity() {
        this.checkNotClosed();
        return this.bytes.capacity();
    }

    public long remaining() {
        this.checkNotClosed();
        return this.bytes.remaining();
    }

    public long limit() {
        this.checkNotClosed();
        return this.bytes.limit();
    }

    public Bytes limit(long limit) {
        this.checkNotClosed();
        return this.bytes.limit(limit);
    }

    @NotNull
    public ByteOrder byteOrder() {
        this.checkNotClosed();
        return this.bytes.byteOrder();
    }

    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        this.bytes.checkEndOfBuffer();
    }

    public Bytes load() {
        this.checkNotClosed();
        return this.bytes.load();
    }

    public void alignPositionAddr(int powerOf2) {
        this.checkNotClosed();
        this.bytes.alignPositionAddr(powerOf2);
    }

    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        this.checkNotClosed();
        return this.bytes.sliceAsByteBuffer(toReuse);
    }
}
