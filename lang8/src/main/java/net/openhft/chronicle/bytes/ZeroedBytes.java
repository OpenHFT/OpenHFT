package net.openhft.chronicle.bytes;

public class ZeroedBytes extends BytesStoreBytes {
    private final UnderflowMode underflowMode;

    public ZeroedBytes(BytesStore store, UnderflowMode underflowMode) {
        super(store);
        this.underflowMode = underflowMode;
    }

    @Override
    public byte readByte() {
        return positionOk(1) ? super.readByte() : (byte) 0;
    }

    private boolean positionOk(int needs) {
        return underflowMode.isRemainingOk(remaining(), needs);
    }

    @Override
    public short readShort() {
        return positionOk(2) ? super.readShort() : (short) 0;
    }

    @Override
    public int readInt() {
        return positionOk(4) ? super.readInt() : 0;
    }

    @Override
    public long readLong() {
        return positionOk(8) ? super.readLong() : 0L;
    }

    @Override
    public float readFloat() {
        return positionOk(4) ? super.readFloat() : 0.0f;
    }

    @Override
    public double readDouble() {
        return positionOk(8) ? super.readDouble() : 0.0;
    }
}
