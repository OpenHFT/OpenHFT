package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by peter.lawrey on 24/02/15.
 */
public class MappedBytes extends AbstractBytes {
    private final MappedFile mappedFile;

    MappedBytes(MappedFile mappedFile) {
        super(NoBytesStore.NO_BYTES_STORE);
        this.mappedFile = mappedFile;
        mappedFile.reserve();
        clear();
    }

    public static MappedBytes mappedBytes(String filename, long chunkSize) throws FileNotFoundException {
        return new MappedBytes(new MappedFile(new RandomAccessFile(filename, "rw"), chunkSize, OS.pageSize()));
    }

    @Override
    public long capacity() {
        return mappedFile == null ? 0L : mappedFile.capacity();
    }

    @Override
    public void reserve() throws IllegalStateException {
        super.reserve();
        mappedFile.reserve();
    }

    @Override
    public void release() throws IllegalStateException {
        super.release();
        mappedFile.release();
    }

    @Override
    public long refCount() {
        return mappedFile.refCount();
    }

    @Override
    protected long checkOffset(long offset) {
        if (!bytesStore.inStore(offset)) {
            bytesStore.release();
            try {
                bytesStore = mappedFile.acquireByteStore(offset);
            } catch (IOException e) {
                throw new IORuntimeException(e);
            }
        }
        return offset;
    }

    @Override
    public Bytes writeLong(long offset, long i) {
        return super.writeLong(offset, i);
    }

    @Override
    public Bytes bytes() {
        throw new UnsupportedOperationException();
    }
}
