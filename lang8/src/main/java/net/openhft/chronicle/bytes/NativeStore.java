package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.ReferenceCounter;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

public class NativeStore implements BytesStore<NativeStore> {
    private static final long MEMORY_MAPPED_SIZE = 128 << 10;
    private final Cleaner cleaner;
    private final ReferenceCounter refCount = ReferenceCounter.onReleased(this::performRelease);

    private volatile Object underlyingObject;

    protected long address;
    private long maximumLimit;

    private NativeStore(ByteBuffer bb) {
        underlyingObject = bb;
        this.address = ((DirectBuffer) bb).address();
        this.maximumLimit = bb.capacity();
        cleaner = null;
    }

    static NativeStore wrap(ByteBuffer bb) {
        return new NativeStore(bb);
    }

    protected NativeStore(long address, long maximumLimit, Runnable deallocator) {
        this.address = address;
        this.maximumLimit = maximumLimit;
        cleaner = Cleaner.create(this, deallocator);
        underlyingObject = null;
    }

    public static NativeStore nativeStore(long capacity) {
        return of(capacity, true);
    }

    public static NativeStore lazyNativeStore(long capacity) {
        return of(capacity, false);
    }

    private static NativeStore of(long capacity, boolean zeroOut) {
        long address = MEMORY.allocate(capacity);
        if (zeroOut || capacity < MEMORY_MAPPED_SIZE) {
            MEMORY.setMemory(address, capacity, (byte) 0);
            MEMORY.storeFence();
        }
        Deallocator deallocator = new Deallocator(address);
        return new NativeStore(address, capacity, deallocator);
    }

    @Override
    public long maximumLimit() {
        return maximumLimit;
    }

    @Override
    public void storeFence() {
        MEMORY.storeFence();
    }

    @Override
    public void loadFence() {
        MEMORY.loadFence();
    }

    @Override
    public void reserve() {
        refCount.reserve();
    }

    @Override
    public void release() {
        refCount.release();
    }

    @Override
    public long refCount() {
        return refCount.get();
    }

    @Override
    public byte readByte(long offset) {
        return MEMORY.readByte(address + translate(offset));
    }

    @Override
    public short readShort(long offset) {
        return MEMORY.readShort(address + translate(offset));
    }

    @Override
    public int readInt(long offset) {
        return MEMORY.readInt(address + translate(offset));
    }

    @Override
    public long readLong(long offset) {
        return MEMORY.readLong(address + translate(offset));
    }

    private long translate(long offset) {
        long offset2 = offset - start();
        if (offset2 < 0 || offset2 >= maximumLimit())
            throw new IllegalArgumentException("Offset out of bounds");
        return offset2;
    }

    @Override
    public float readFloat(long offset) {
        return MEMORY.readFloat(address + translate(offset));
    }

    @Override
    public double readDouble(long offset) {
        return MEMORY.readDouble(address + translate(offset));
    }

    @Override
    public NativeStore writeByte(long offset, byte i8) {
        MEMORY.writeByte(address + translate(offset), i8);
        return this;
    }

    @Override
    public NativeStore writeShort(long offset, short i16) {
        MEMORY.writeShort(address + translate(offset), i16);
        return this;
    }

    @Override
    public NativeStore writeInt(long offset, int i32) {
        MEMORY.writeInt(address + translate(offset), i32);
        return this;
    }

    @Override
    public NativeStore writeOrderedInt(long offset, int i) {
        MEMORY.writeOrderedInt(address + translate(offset), i);
        return this;
    }

    @Override
    public NativeStore writeLong(long offset, long i64) {
        MEMORY.writeLong(address + translate(offset), i64);
        return this;
    }

    @Override
    public NativeStore writeFloat(long offset, float f) {
        MEMORY.writeFloat(address + translate(offset), f);
        return this;
    }

    @Override
    public NativeStore writeDouble(long offset, double d) {
        MEMORY.writeDouble(address + translate(offset), d);
        return this;
    }

    @Override
    public NativeStore write(long offsetInRDO, byte[] bytes, int offset, int length) {
        MEMORY.copyMemory(bytes, offset, address + translate(offsetInRDO), length);
        return this;
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) {
        return MEMORY.compareAndSwapInt(address + translate(offset), expected, value);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) {
        return MEMORY.compareAndSwapLong(address + translate(offset), expected, value);
    }

    @Override
    public NativeStore writeOrderedLong(long offset, long i) {
        MEMORY.writeOrderedLong(address + translate(offset), i);
        return this;
    }

    @Override
    public NativeStore write(long offsetInRDO, ByteBuffer bytes, int offset, int length) {
        if (bytes.isDirect()) {
            MEMORY.copyMemory(((DirectBuffer) bytes).address(), address + translate(offsetInRDO), length);
        } else {
            MEMORY.copyMemory(bytes.array(), offset, address + translate(offsetInRDO), length);
        }
        return this;
    }

    protected void performRelease() {
        cleaner.clean();
    }

    static class Deallocator implements Runnable {
        private volatile long address;

        Deallocator(long address) {
            assert address != 0;
            this.address = address;
        }

        @Override
        public void run() {
            if (address == 0)
                return;
            address = 0;
            MEMORY.freeMemory(address);
        }
    }
}
