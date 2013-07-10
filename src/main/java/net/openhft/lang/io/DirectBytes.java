package net.openhft.lang.io;

import sun.misc.Cleaner;

/**
 * @author peter.lawrey
 */
public class DirectBytes extends NativeBytes {
    private final Cleaner cleaner = Cleaner.create(this, new Runnable() {
        @Override
        public void run() {
            NativeBytes.UNSAFE.freeMemory(startAddr);
            startAddr = positionAddr = limitAddr = 0;
        }
    });

    public static DirectBytes allocate(long size) {
        return new DirectBytes(null, size);
    }

    public DirectBytes(BytesMarshallerFactory bytesMarshallerFactory, long size) {
        super(bytesMarshallerFactory, 0, 0, 0);
        startAddr = positionAddr = NativeBytes.UNSAFE.allocateMemory(size);
        limitAddr = startAddr + size;
    }

    public void resize(long newSize) {
        if (newSize == capacity())
            return;
        long position = position();
        if (position > newSize)
            position = newSize;
        startAddr = NativeBytes.UNSAFE.reallocateMemory(startAddr, newSize);
        positionAddr = startAddr + position;
        limitAddr = startAddr + newSize;
    }

    public void free() {
        cleaner.clean();
    }
}
