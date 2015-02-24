package net.openhft.chronicle.bytes;

public class BytesStoreBytes extends AbstractBytes {
    public BytesStoreBytes(BytesStore bytesStore) {
        super(bytesStore);
    }

    public void setBytesStore(BytesStore bytesStore) {
        BytesStore oldBS = this.bytesStore;
        this.bytesStore = bytesStore;
        oldBS.release();
        clear();
    }

}
