package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.ReferenceCounted;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedFile implements ReferenceCounted {
    private final AtomicInteger refCount = new AtomicInteger(1);
    private final FileChannel fileChannel;
    private final long chunkSize;
    private final long overlapSize;

    public MappedFile(FileChannel fileChannel, long chunkSize, long overlapSize) {
        this.fileChannel = fileChannel;
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    public static MappedFile of(String filename) throws FileNotFoundException {
        return of(filename, 64 << 20);
    }

    public static MappedFile of(String filename, long chunkSize) throws FileNotFoundException {
        return of(filename, chunkSize, chunkSize / 4);
    }

    private static MappedFile of(String filename, long chunkSize, long overlapSize) throws FileNotFoundException {
        return new MappedFile(new RandomAccessFile(filename, "rw").getChannel(), chunkSize, overlapSize);
    }

    @Override
    public void reserve() {
        refCount.incrementAndGet();
    }

    @Override
    public void release() {
        refCount.decrementAndGet();
    }

    @Override
    public int refCount() {
        return refCount.get();
    }
}
