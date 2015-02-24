package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by peter.lawrey on 24/02/15.
 */
public class MappedBytes extends AbstractBytes {
    private final MappedFile mappedFile;

    // assume the mapped file is reserved already.
    MappedBytes(MappedFile mappedFile) {
        super(NoBytesStore.NO_BYTES_STORE);
        this.mappedFile = mappedFile;
        clear();
    }

    public static MappedBytes mappedBytes(String filename, long chunkSize) throws FileNotFoundException {
        return mappedBytes(new File(filename), chunkSize);
    }

    public static MappedBytes mappedBytes(File file, long chunkSize) throws FileNotFoundException {
        MappedFile rw = new MappedFile(new RandomAccessFile(file, "rw"), chunkSize, OS.pageSize());
        MappedBytes bytes = new MappedBytes(rw);
        return bytes;
    }

    @Override
    public long maximumLimit() {
        return mappedFile == null ? 0L : mappedFile.capacity();
    }

    @Override
    public void reserve() throws IllegalStateException {
        super.reserve();
    }

    @Override
    public void release() throws IllegalStateException {
        super.release();
    }

    @Override
    public long refCount() {
        return Math.max(super.refCount(), mappedFile.refCount());
    }

    @Override
    protected long checkOffset(long offset, int adding) {
        if (!bytesStore.inStore(offset)) {
            BytesStore oldBS = bytesStore;
            try {
                bytesStore = mappedFile.acquireByteStore(offset);
                oldBS.release();
            } catch (IOException e) {
                throw new IORuntimeException(e);
            }
        }
        return offset;
    }

    @Override
    public long start() {
        return 0L;
    }

    @Override
    public Bytes writeLong(long offset, long i) {
        return super.writeLong(offset, i);
    }

    @Override
    protected void performRelease() {
        super.performRelease();
        mappedFile.close();
    }

    @Override
    public Bytes bytes() {
        throw new UnsupportedOperationException();
    }
}
