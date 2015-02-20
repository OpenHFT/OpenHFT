package net.openhft.bytes;

public class NativeStore implements BytesStore<NativeStore> {
    private final long capacity;

    private NativeStore(long capacity) {
        this.capacity = capacity;
    }

    public static NativeStore of(long capacity) {
        return new NativeStore(capacity);
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public void storeFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readUnsignedByte(long position) {
        throw new UnsupportedOperationException();
    }


    @Override
    public NativeStore writeOrderedInt(long offset, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reserve() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int refCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NativeStore writeByte(long offset, byte i8) {
        throw new UnsupportedOperationException();
    }
}
