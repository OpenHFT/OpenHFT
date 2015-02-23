package net.openhft.lang.io;

import net.openhft.lang.io.serialization.ObjectSerializer;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.constraints.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;

/**
 * A super class for those which wrap bytes.
 */
public abstract class WrappedBytes<B extends Bytes> implements Bytes {
    protected B wrapped;

    protected WrappedBytes(B wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void clearThreadAssociation() {
        wrapped.clearThreadAssociation();
    }

    public ByteBuffer sliceAsByteBuffer(@org.jetbrains.annotations.Nullable ByteBuffer toReuse) {
        return wrapped.sliceAsByteBuffer(toReuse);
    }

    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        wrapped.readMarshallable(in);
    }

    @Override
    public String toDebugString(long limit) {
        return wrapped.toDebugString(limit);
    }

    @Override
    public boolean compare(long offset, RandomDataInput input, long inputOffset, long len) {
        return wrapped.compare(offset, input, inputOffset, len);
    }

    public long readCompactLong() {
        return wrapped.readCompactLong();
    }

    public boolean tryLockNanosInt(long offset, long nanos) {
        return wrapped.tryLockNanosInt(offset, nanos);
    }

    public void writeMarshallable(@NotNull Bytes out) {
        wrapped.writeMarshallable(out);
    }

    public int readInt24() {
        return wrapped.readInt24();
    }

    public void flush() {
        wrapped.flush();
    }

    public void writeDouble(long offset, double v) {
        wrapped.writeDouble(offset, v);
    }

    public long limit() {
        return wrapped.limit();
    }

    @NotNull
    public ByteStringAppender appendTimeMillis(long timeInMS) {
        return wrapped.appendTimeMillis(timeInMS);
    }

    @org.jetbrains.annotations.Nullable
    public <E extends Enum<E>> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester) throws BufferUnderflowException {
        return wrapped.parseEnum(eClass, tester);
    }

    public int refCount() {
        return wrapped.refCount();
    }

    public void writeShort(long offset, int v) {
        wrapped.writeShort(offset, v);
    }

    public <E> void writeEnum(@org.jetbrains.annotations.Nullable E e) {
        wrapped.writeEnum(e);
    }

    @NotNull
    public <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator) {
        return wrapped.append(list, separator);
    }

    public void writeCompactUnsignedShort(int v) {
        wrapped.writeCompactUnsignedShort(v);
    }

    public long readVolatileLong() {
        return wrapped.readVolatileLong();
    }

    public void write(RandomDataInput in, long position, long length) {
        wrapped.write(in, position, length);
    }

    public void write(Byteable byteable) {
        wrapped.write(byteable);
    }

    public void writeOrderedInt(int v) {
        wrapped.writeOrderedInt(v);
    }

    public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
        return wrapped.readUTFΔ(stringBuilder);
    }

    public void writeInt48(long offset, long v) {
        wrapped.writeInt48(offset, v);
    }

    public long readLong() {
        return wrapped.readLong();
    }

    public void writeLong(long v) {
        wrapped.writeLong(v);
    }

    @NotNull
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        return wrapped.appendDateTimeMillis(timeInMS);
    }

    @org.jetbrains.annotations.Nullable
    public <E> E readEnum(@NotNull Class<E> eClass) {
        return wrapped.readEnum(eClass);
    }

    public void write(RandomDataInput in) {
        wrapped.write(in);
    }

    @NotNull
    public ByteStringAppender append(double d) {
        return wrapped.append(d);
    }

    @NotNull
    public String toDebugString() {
        return wrapped.toDebugString();
    }

    public boolean isFinished() {
        return wrapped.isFinished();
    }

    public void writeCompactUnsignedInt(long v) {
        wrapped.writeCompactUnsignedInt(v);
    }

    @NotNull
    public ByteStringAppender append(double d, int precision) {
        return wrapped.append(d, precision);
    }

    public int readUnsignedByteOrThrow() throws BufferUnderflowException {
        return wrapped.readUnsignedByteOrThrow();
    }

    public Bytes zeroOut(long start, long end) {
        wrapped.zeroOut(start, end);
        return this;
    }

    @Override
    public Bytes zeroOut(long start, long end, boolean ifNotZero) {
        wrapped.zeroOut(start, end, ifNotZero);
        return this;
    }

    public void writeShort(int v) {
        wrapped.writeShort(v);
    }

    public short addShort(long offset, short s) {
        return wrapped.addShort(offset, s);
    }

    public void writeUnsignedInt(long v) {
        wrapped.writeUnsignedInt(v);
    }

    public void free() {
        wrapped.free();
    }

    public int readUnsignedShort() {
        return wrapped.readUnsignedShort();
    }

    public void writeStopBit(long n) {
        wrapped.writeStopBit(n);
    }

    @org.jetbrains.annotations.Nullable
    public <T> T readObject(Class<T> tClass) throws IllegalStateException {
        return wrapped.readObject(tClass);
    }

    public void writeCompactInt(int v) {
        wrapped.writeCompactInt(v);
    }

    public void writeOrderedLong(long v) {
        wrapped.writeOrderedLong(v);
    }

    public byte addByte(long offset, byte b) {
        return wrapped.addByte(offset, b);
    }

    public int readVolatileInt() {
        return wrapped.readVolatileInt();
    }

    public void close() {
        wrapped.close();
    }

    public void read(@NotNull ByteBuffer bb) {
        wrapped.read(bb);
    }

    public void read(@NotNull ByteBuffer bb, int length) {
        wrapped.read(bb, length);
    }

    @NotNull
    public ByteStringAppender append(long l, int base) {
        return wrapped.append(l, base);
    }

    public long skip(long n) {
        return wrapped.skip(n);
    }

    public boolean selfTerminating() {
        return wrapped.selfTerminating();
    }

    public void writeBytes(@NotNull String s) {
        wrapped.writeBytes(s);
    }

    public long size() {
        return wrapped.size();
    }

    public int readCompactUnsignedShort() {
        return wrapped.readCompactUnsignedShort();
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence s, int start, int end) {
        return wrapped.append(s, start, end);
    }

    public void writeCompactLong(long v) {
        wrapped.writeCompactLong(v);
    }

    public double readCompactDouble() {
        return wrapped.readCompactDouble();
    }

    public void writeOrderedInt(long offset, int v) {
        wrapped.writeOrderedInt(offset, v);
    }

    public void writeObject(Object object, int start, int end) {
        wrapped.writeObject(object, start, end);
    }

    public CharSequence asString() {
        return wrapped.asString();
    }

    @org.jetbrains.annotations.Nullable
    public String readUTFΔ() {
        return wrapped.readUTFΔ();
    }

    public Bytes flip() {
        return wrapped.flip();
    }

    public int addInt(long offset, int i) {
        return wrapped.addInt(offset, i);
    }

    public long readUnsignedInt(long offset) {
        return wrapped.readUnsignedInt(offset);
    }

    public void writeByte(int v) {
        wrapped.writeByte(v);
    }

    public void writeUnsignedInt(long offset, long v) {
        wrapped.writeUnsignedInt(offset, v);
    }

    public void writeInt(int v) {
        wrapped.writeInt(v);
    }

    public short readShort() {
        return wrapped.readShort();
    }

    public void writeUnsignedByte(long offset, int v) {
        wrapped.writeUnsignedByte(offset, v);
    }

    public void asString(Appendable appendable) {
        wrapped.asString(appendable);
    }

    public long readInt48(long offset) {
        return wrapped.readInt48(offset);
    }

    public void unlockRWReadLock(long offset) throws IllegalStateException {
        wrapped.unlockRWReadLock(offset);
    }

    @NotNull
    public String readUTF() {
        return wrapped.readUTF();
    }

    public void writeUnsignedShort(long offset, int v) {
        wrapped.writeUnsignedShort(offset, v);
    }

    public void readFully(@NotNull char[] data) {
        wrapped.readFully(data);
    }

    public void writeInt24(long offset, int v) {
        wrapped.writeInt24(offset, v);
    }

    public void writeChars(@NotNull CharSequence cs) {
        wrapped.writeChars(cs);
    }

    public float readFloat(long offset) {
        return wrapped.readFloat(offset);
    }

    public long capacity() {
        return wrapped.capacity();
    }

    public CharSequence subSequence(int start, int end) {
        return wrapped.subSequence(start, end);
    }

    public Bytes clear() {
        return wrapped.clear();
    }

    @org.jetbrains.annotations.Nullable
    public String readUTFΔ(long offset) throws IllegalStateException {
        return wrapped.readUTFΔ(offset);
    }

    @NotNull
    public ObjectSerializer objectSerializer() {
        return wrapped.objectSerializer();
    }

    public void writeOrderedLong(long offset, long v) {
        wrapped.writeOrderedLong(offset, v);
    }

    public long addAtomicLong(long offset, long l) {
        return wrapped.addAtomicLong(offset, l);
    }

    @NotNull
    public ByteStringAppender append(char c) {
        return wrapped.append(c);
    }

    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        wrapped.busyLockInt(offset);
    }

    public void resetLockInt(long offset) {
        wrapped.resetLockInt(offset);
    }

    @org.jetbrains.annotations.Nullable
    public String readLine() {
        return wrapped.readLine();
    }

    public char readChar(long offset) {
        return wrapped.readChar(offset);
    }

    @org.jetbrains.annotations.Nullable
    public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
        return wrapped.readInstance(objClass, obj);
    }

    @NotNull
    public ByteStringAppender append(boolean b) {
        return wrapped.append(b);
    }

    public int addUnsignedByte(long offset, int i) {
        return wrapped.addUnsignedByte(offset, i);
    }

    public void readFully(@NotNull byte[] byteArray, int off, int len) {
        wrapped.readFully(byteArray, off, len);
    }

    public void readFully(@NotNull char[] data, int off, int len) {
        wrapped.readFully(data, off, len);
    }

    public int addAndGetInt(long offset, int delta) {
        return wrapped.addAndGetInt(offset, delta);
    }


    public long addUnsignedInt(long offset, long i) {
        return wrapped.addUnsignedInt(offset, i);
    }

    public void writeInt48(long v) {
        wrapped.writeInt48(v);
    }

    @NotNull
    public ByteStringAppender append(@NotNull MutableDecimal md) {
        return wrapped.append(md);
    }

    public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        return wrapped.readMap(map, kClass, vClass);
    }

    public char charAt(int index) {
        return wrapped.charAt(index);
    }

    public void writeOrderedFloat(long offset, float v) {
        wrapped.writeOrderedFloat(offset, v);
    }

    public void unlockRWWriteLock(long offset) throws IllegalStateException {
        wrapped.unlockRWWriteLock(offset);
    }

    public void parseUTF(@NotNull StringBuilder builder, @NotNull StopCharTester tester) throws BufferUnderflowException {
        wrapped.parseUTF(builder, tester);
    }

    @NotNull
    public InputStream inputStream() {
        return wrapped.inputStream();
    }

    public long remaining() {
        return wrapped.remaining();
    }

    public void writeByte(long offset, int b) {
        wrapped.writeByte(offset, b);
    }

    public double readDouble() {
        return wrapped.readDouble();
    }

    public int readCompactInt() {
        return wrapped.readCompactInt();
    }

    public boolean release() {
        return wrapped.release();
    }

    public boolean readBoolean(long offset) {
        return wrapped.readBoolean(offset);
    }

    public void writeBoolean(boolean v) {
        wrapped.writeBoolean(v);
    }

    public int read(@NotNull byte[] byteArray) {
        return wrapped.read(byteArray);
    }

    public void writeChars(@NotNull String s) {
        wrapped.writeChars(s);
    }

    public Bytes slice() {
        return wrapped.slice();
    }

    public Bytes zeroOut() {
        return wrapped.zeroOut();
    }

    public void toString(Appendable sb, long start, long position, long end) {
        wrapped.toString(sb, start, position, end);
    }

    public void writeOrderedDouble(long offset, double v) {
        wrapped.writeOrderedDouble(offset, v);
    }

    public long readStopBit() {
        return wrapped.readStopBit();
    }

    public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {
        wrapped.busyLockLong(offset);
    }

    public void writeDouble(double v) {
        wrapped.writeDouble(v);
    }

    public double readDouble(long offset) {
        return wrapped.readDouble(offset);
    }

    public float addFloat(long offset, float f) {
        return wrapped.addFloat(offset, f);
    }

    public boolean skipTo(@NotNull StopCharTester tester) {
        return wrapped.skipTo(tester);
    }

    public void writeChar(int v) {
        wrapped.writeChar(v);
    }

    public void writeInt(long offset, int v) {
        wrapped.writeInt(offset, v);
    }

    @NotNull
    public OutputStream outputStream() {
        return wrapped.outputStream();
    }

    public boolean compareAndSwapDouble(long offset, double expected, double x) {
        return wrapped.compareAndSwapDouble(offset, expected, x);
    }

    public File file() {
        return wrapped.file();
    }

    public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {
        wrapped.readList(list, eClass);
    }

    public void writeUnsignedByte(int v) {
        wrapped.writeUnsignedByte(v);
    }

    public int readInt24(long offset) {
        return wrapped.readInt24(offset);
    }

    public long readInt48() {
        return wrapped.readInt48();
    }

    public void write(@NotNull char[] data) {
        wrapped.write(data);
    }

    @org.jetbrains.annotations.Nullable
    public Object readObject() throws IllegalStateException {
        return wrapped.readObject();
    }

    @NotNull
    public ByteStringAppender append(@net.openhft.lang.model.constraints.Nullable Enum value) {
        return wrapped.append(value);
    }

    @NotNull
    public String parseUTF(@NotNull StopCharTester tester) throws BufferUnderflowException {
        return wrapped.parseUTF(tester);
    }

    public int readInt() {
        return wrapped.readInt();
    }

    public void write(@NotNull char[] data, int off, int len) {
        wrapped.write(data, off, len);
    }

    public int addUnsignedShort(long offset, int i) {
        return wrapped.addUnsignedShort(offset, i);
    }

    public float readFloat() {
        return wrapped.readFloat();
    }

    public int available() {
        return wrapped.available();
    }

    public long position() {
        return wrapped.position();
    }

    public double addDouble(long offset, double d) {
        return wrapped.addDouble(offset, d);
    }

    public void write(int b) {
        wrapped.write(b);
    }

    public int skipBytes(int n) {
        return wrapped.skipBytes(n);
    }

    public short readCompactShort() {
        return wrapped.readCompactShort();
    }

    public void write(long offset, byte[] byteArray) {
        wrapped.write(offset, byteArray);
    }

    public <E> void writeList(@NotNull Collection<E> list) {
        wrapped.writeList(list);
    }

    public int read(@NotNull byte[] byteArray, int off, int len) {
        return wrapped.read(byteArray, off, len);
    }

    public int readInt(long offset) {
        return wrapped.readInt(offset);
    }

    public void writeFloat(long offset, float v) {
        wrapped.writeFloat(offset, v);
    }

    public long parseLong() throws BufferUnderflowException {
        return wrapped.parseLong();
    }

    public int readUnsignedByte(long offset) {
        return wrapped.readUnsignedByte(offset);
    }

    public Bytes slice(long offset, long length) {
        return wrapped.slice(offset, length);
    }

    public void writeObject(@org.jetbrains.annotations.Nullable Object object) {
        wrapped.writeObject(object);
    }

    public int length() {
        return wrapped.length();
    }

    public char readChar() {
        return wrapped.readChar();
    }

    public int read() {
        return wrapped.read();
    }

    public void writeBoolean(long offset, boolean v) {
        wrapped.writeBoolean(offset, v);
    }

    public double parseDouble() throws BufferUnderflowException {
        return wrapped.parseDouble();
    }

    public void writeCompactDouble(double v) {
        wrapped.writeCompactDouble(v);
    }

    public float addAtomicFloat(long offset, float f) {
        return wrapped.addAtomicFloat(offset, f);
    }

    public void selfTerminating(boolean selfTerminate) {
        wrapped.selfTerminating(selfTerminate);
    }

    public long readCompactUnsignedInt() {
        return wrapped.readCompactUnsignedInt();
    }

    public double readVolatileDouble(long offset) {
        return wrapped.readVolatileDouble(offset);
    }

    public long addLong(long offset, long i) {
        return wrapped.addLong(offset, i);
    }

    public long readLong(long offset) {
        return wrapped.readLong(offset);
    }

    public boolean compareAndSwapInt(long offset, int expected, int x) {
        return wrapped.compareAndSwapInt(offset, expected, x);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence s) {
        return wrapped.append(s);
    }

    @NotNull
    public ByteStringAppender append(int i) {
        return wrapped.append(i);
    }

    public <K, V> void writeMap(@NotNull Map<K, V> map) {
        wrapped.writeMap(map);
    }

    public Boolean parseBoolean(@NotNull StopCharTester tester) throws BufferUnderflowException {
        return wrapped.parseBoolean(tester);
    }

    public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return wrapped.tryRWReadLock(offset, timeOutNS);
    }

    public int readUnsignedShort(long offset) {
        return wrapped.readUnsignedShort(offset);
    }

    public void writeUTFΔ(long offset, int maxSize, @org.jetbrains.annotations.Nullable CharSequence s) throws IllegalStateException {
        wrapped.writeUTFΔ(offset, maxSize, s);
    }

    public byte readByte(long offset) {
        return wrapped.readByte(offset);
    }

    @NotNull
    public ByteStringAppender append(long l) {
        return wrapped.append(l);
    }

    public void writeUTFΔ(@org.jetbrains.annotations.Nullable CharSequence s) {
        wrapped.writeUTFΔ(s);
    }

    public boolean compareAndSwapLong(long offset, long expected, long x) {
        return wrapped.compareAndSwapLong(offset, expected, x);
    }

    public void writeCompactShort(int v) {
        wrapped.writeCompactShort(v);
    }

    public Bytes bytes() {
        return wrapped.bytes();
    }

    public void write(byte[] byteArray) {
        wrapped.write(byteArray);
    }

    public void unlockInt(long offset) throws IllegalMonitorStateException {
        wrapped.unlockInt(offset);
    }

    public boolean tryLockLong(long offset) {
        return wrapped.tryLockLong(offset);
    }

    public byte readByte() {
        return wrapped.readByte();
    }

    public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException {
        return wrapped.tryRWWriteLock(offset, timeOutNS);
    }

    public void write(byte[] byteArray, int off, int len) {
        wrapped.write(byteArray, off, len);
    }

    public void writeUTF(@NotNull String s) {
        wrapped.writeUTF(s);
    }

    public Bytes load() {
        return wrapped.load();
    }

    public int getAndAdd(long offset, int delta) {
        return wrapped.getAndAdd(offset, delta);
    }

    public short readShort(long offset) {
        return wrapped.readShort(offset);
    }

    public boolean stepBackAndSkipTo(@NotNull StopCharTester tester) {
        return wrapped.stepBackAndSkipTo(tester);
    }

    public void resetLockLong(long offset) {
        wrapped.resetLockLong(offset);
    }

    public int readVolatileInt(long offset) {
        return wrapped.readVolatileInt(offset);
    }

    @NotNull
    public ByteOrder byteOrder() {
        return wrapped.byteOrder();
    }

    public Bytes bytes(long offset, long length) {
        return wrapped.bytes(offset, length);
    }

    public void alignPositionAddr(int alignment) {
        wrapped.alignPositionAddr(alignment);
    }

    public void writeUnsignedShort(int v) {
        wrapped.writeUnsignedShort(v);
    }

    public long parseLong(int base) throws BufferUnderflowException {
        return wrapped.parseLong(base);
    }

    public boolean readBoolean() {
        return wrapped.readBoolean();
    }

    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        wrapped.checkEndOfBuffer();
    }

    public float readVolatileFloat(long offset) {
        return wrapped.readVolatileFloat(offset);
    }

    @NotNull
    public MutableDecimal parseDecimal(@NotNull MutableDecimal decimal) throws BufferUnderflowException {
        return wrapped.parseDecimal(decimal);
    }

    public double addAtomicDouble(long offset, double d) {
        return wrapped.addAtomicDouble(offset, d);
    }

    public void unlockLong(long offset) throws IllegalMonitorStateException {
        wrapped.unlockLong(offset);
    }

    public void writeFloat(float v) {
        wrapped.writeFloat(v);
    }

    public void reserve() {
        wrapped.reserve();
    }

    public void write(@NotNull ByteBuffer bb) {
        wrapped.write(bb);
    }

    public long threadIdForLockLong(long offset) {
        return wrapped.threadIdForLockLong(offset);
    }

    public void writeChar(long offset, int v) {
        wrapped.writeChar(offset, v);
    }

    public boolean tryLockNanosLong(long offset, long nanos) {
        return wrapped.tryLockNanosLong(offset, nanos);
    }

    public int addAtomicInt(long offset, int i) {
        return wrapped.addAtomicInt(offset, i);
    }

    public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {
        wrapped.writeInstance(objClass, obj);
    }

    public void readFully(@NotNull byte[] byteArray) {
        wrapped.readFully(byteArray);
    }

    public Bytes position(long position) {
        return wrapped.position(position);
    }

    public void writeLong(long offset, long v) {
        wrapped.writeLong(offset, v);
    }

    public void readObject(Object object, int start, int end) {
        wrapped.readObject(object, start, end);
    }

    public int threadIdForLockInt(long offset) {
        return wrapped.threadIdForLockInt(offset);
    }

    @NotNull
    public ByteStringAppender appendDateMillis(long timeInMS) {
        return wrapped.appendDateMillis(timeInMS);
    }

    public void writeInt24(int v) {
        wrapped.writeInt24(v);
    }

    public boolean startsWith(RandomDataInput keyBytes) {
        return wrapped.startsWith(keyBytes);
    }

    public long readUnsignedInt() {
        return wrapped.readUnsignedInt();
    }

    public Bytes limit(long limit) {
        return wrapped.limit(limit);
    }

    public void finish() {
        wrapped.finish();
    }

    public long address() {
        return wrapped.address();
    }

    public boolean tryLockInt(long offset) {
        return wrapped.tryLockInt(offset);
    }

    public long readVolatileLong(long offset) {
        return wrapped.readVolatileLong(offset);
    }

    public int readUnsignedByte() {
        return wrapped.readUnsignedByte();
    }

    @Override
    public void readFully(long offset, @org.jetbrains.annotations.NotNull byte[] byteArray, int off, int len) {
        wrapped.readFully(offset, byteArray, off, len);
    }

    @Override
    public void write(long offset, byte[] byteArray, int off, int len) {
        wrapped.write(offset, byteArray, off, len);
    }

    @Override
    public void write(long offset, Bytes bytes) {
        wrapped.write(offset, bytes);
    }
}
