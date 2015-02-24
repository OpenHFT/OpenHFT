package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.ReferenceCounted;
import net.openhft.chronicle.core.ReferenceCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MappedFile implements ReferenceCounted {
    private final ReferenceCounter refCount = ReferenceCounter.onReleased(this::performRelease);

    private final RandomAccessFile raf;

    private final FileChannel fileChannel;
    private final long chunkSize;
    private final long overlapSize;
    private final List<WeakReference<MappedBytesStore>> stores = new ArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final ThreadLocal<WeakReference<Bytes>> threadLocalBytes = new ThreadLocal<>();
    private final long capacity;

    MappedFile(RandomAccessFile raf, long chunkSize, long overlapSize) {
        this.raf = raf;
        this.fileChannel = raf.getChannel();
        this.chunkSize = Maths.nextPower2(chunkSize, OS.pageSize());
        this.overlapSize = overlapSize == 0 ? 0 : Maths.nextPower2(overlapSize, OS.pageSize());
        capacity = 1L << 40;
    }

    public static MappedFile mappedFile(String filename, long chunkSize) throws FileNotFoundException {
        return mappedFile(filename, chunkSize, OS.pageSize());
    }

    public static MappedFile mappedFile(String filename, long chunkSize, long overlapSize) throws FileNotFoundException {
        return mappedFile(new File(filename), chunkSize, overlapSize);
    }

    public static MappedFile mappedFile(File file, long chunkSize, long overlapSize) throws FileNotFoundException {
        return new MappedFile(new RandomAccessFile(file, "rw"), chunkSize, overlapSize);
    }

    public MappedBytesStore acquireByteStore(long position) throws IOException {
        if (closed.get())
            throw new IOException("Closed");
        int chunk = (int) (position / chunkSize);
        synchronized (stores) {
            while (stores.size() <= chunk) {
                stores.add(null);
            }
            WeakReference<MappedBytesStore> mbsRef = stores.get(chunk);
            if (mbsRef != null) {
                MappedBytesStore mbs = mbsRef.get();
                if (mbs != null && mbs.tryReserve()) {
                    return mbs;
                }
            }
            long minSize = (chunk + 1L) * chunkSize + overlapSize;
            long size = fileChannel.size();
            if (size < minSize) {
                // handle a possible race condition between processes.
                try (FileLock lock = fileChannel.lock()) {
                    size = fileChannel.size();
                    if (size < minSize) {
                        raf.setLength(minSize);
                    }
                }
            }
            long mappedSize = chunkSize + overlapSize;
            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, chunk * chunkSize, mappedSize);
            MappedBytesStore mbs2 = new MappedBytesStore(this, chunk * chunkSize, address, mappedSize, chunkSize);
            stores.set(chunk, new WeakReference<>(mbs2));
            mbs2.reserve();
            return mbs2;
        }
    }

    /**
     * Convenience method so you don't need to release the BytesStore
     */

    public Bytes acquireBytes(long position) throws IOException {
        MappedBytesStore mbs = acquireByteStore(position);
        Bytes bytes = mbs.bytes();
        mbs.release();
        return bytes;
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

    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        synchronized (stores) {
            ReferenceCounted.releaseAll((List) stores);
        }
        refCount.release();
    }

    void performRelease() {
        for (int i = 0; i < stores.size(); i++) {
            WeakReference<MappedBytesStore> storeRef = stores.get(i);
            if (storeRef == null)
                continue;
            MappedBytesStore mbs = storeRef.get();
            if (mbs != null) {
                long count = mbs.refCount();
                if (count > 0) {
                    mbs.release();
                    if (count > 1)
                        continue;
                }
            }
            stores.set(i, null);
        }
        try {
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String referenceCounts() {
        StringBuilder sb = new StringBuilder();
        sb.append("refCount: ").append(refCount());
        for (WeakReference<MappedBytesStore> store : stores) {
            long count = 0;
            if (store != null) {
                MappedBytesStore mbs = store.get();
                if (mbs != null)
                    count = mbs.refCount();
            }
            sb.append(", ").append(count);
        }
        return sb.toString();
    }

    public Bytes bytes() {
        return new MappedBytes(this);
    }

    public Bytes bytesThreadLocal() {
        WeakReference<Bytes> bytesRef = threadLocalBytes.get();
        if (bytesRef != null) {
            Bytes bytes = bytesRef.get();
            if (bytes != null)
                return bytes;
        }
        Bytes bytes = bytes();
        threadLocalBytes.set(new WeakReference<>(bytes));
        return bytes;
    }

    public long capacity() {
        return capacity;
    }
}
