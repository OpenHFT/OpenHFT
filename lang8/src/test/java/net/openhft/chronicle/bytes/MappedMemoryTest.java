package net.openhft.chronicle.bytes;


import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static net.openhft.chronicle.bytes.MappedBytes.mappedBytes;
import static net.openhft.chronicle.bytes.MappedFile.mappedFile;
import static org.junit.Assert.assertEquals;


public class MappedMemoryTest {

    private static final long SHIFT = 27L;
    private static long BLOCK_SIZE = 1L << SHIFT;

    @Test
    public void withMappedNativeBytesTest() throws IOException {

        for (int t = 0; t < 5; t++) {
            File tempFile = File.createTempFile("chronicle", "q");
            try {

                final Bytes bytes = mappedBytes(tempFile.getName(), BLOCK_SIZE);
                bytes.writeLong(1, 1);
                long startTime = System.nanoTime();
                for (long i = 0; i < BLOCK_SIZE; i += 8) {
                    bytes.writeLong(i);
                }
                bytes.release();
                assertEquals(0, bytes.refCount());
                System.out.println("With MappedNativeBytes,\t avg time= " + 80 * (System.nanoTime() - startTime) / BLOCK_SIZE / 10.0 + " ns, number of longs written=" + BLOCK_SIZE / 8);

            } finally {
                tempFile.delete();
            }
        }
    }

    @Test
    public void withRawNativeBytesTess() throws IOException {

        for (int t = 0; t < 5; t++) {
            File tempFile = File.createTempFile("chronicle", "q");
            try {

                MappedFile mappedFile = mappedFile(tempFile.getName(), BLOCK_SIZE, OS.pageSize());
                Bytes bytes = mappedFile.acquireBytes(1);

                long startTime = System.nanoTime();
                for (long i = 0; i < BLOCK_SIZE; i += 8L) {
                    bytes.writeLong(i);
                }

                bytes.release();
                mappedFile.close();
                assertEquals(mappedFile.referenceCounts(), 0, mappedFile.refCount());
                System.out.println("With NativeBytes,\t\t time= " + 80 * (System.nanoTime() - startTime) / BLOCK_SIZE / 10.0 + " ns, number of longs written=" + BLOCK_SIZE / 8);
            } finally {
                tempFile.delete();
            }
        }
    }
    /*

    @Ignore
    @Test
    public void testShowComparablePerformanceOfBytes() throws IOException {

        for (int x = 0; x < 5; x++) {

            System.out.println("\n\niteration " + x);
            File tempFile = File.createTempFile("chronicle", "q");
            try {

                final MappedFile mappedFile = new MappedFile(tempFile.getName(), BLOCK_SIZE, 8);
                final MappedNativeBytes bytes = new MappedNativeBytes(mappedFile, true);
                bytes.writeLong(1, 1);
                long startTime = System.nanoTime();
                for (long i = 0; i < BLOCK_SIZE; i++) {
                    bytes.writeByte('X');
                }

                System.out.println("With MappedNativeBytes,\t time=" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + ("ms, number of bytes written= 1L << " + SHIFT + " = " + BLOCK_SIZE));

            } finally {
                tempFile.delete();
            }


            File tempFile2 = File.createTempFile("chronicle", "q");
            try {

                MappedFile mappedFile = new MappedFile(tempFile.getName(), BLOCK_SIZE, 8);
                Bytes bytes1 = mappedFile.acquireByteStore(1).bytes();


                long startTime = System.nanoTime();
                for (long i = 0; i < BLOCK_SIZE; i++) {
                    bytes1.writeByte('X');
                }

                System.out.println("With NativeBytes,\t\t time=" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + ("ms, number of bytes written= 1L << " + SHIFT + " = " + BLOCK_SIZE));


            } finally {
                tempFile2.delete();
            }
            System.out.println("");
        }


    }

    @Test
    public void mappedMemoryTest() throws IOException {

        File tempFile = File.createTempFile("chronicle", "q");
        try {

            final MappedFile mappedFile = new MappedFile(tempFile.getName(), BLOCK_SIZE, 8);
            final MappedNativeBytes bytes = new MappedNativeBytes(mappedFile, true);
            bytes.writeUTF("hello this is some very long text");

            bytes.clear();

            bytes.position(100);
            bytes.writeUTF("hello this is some more long text...................");

            bytes.position(100);
            System.out.println("result=" + bytes.readUTF());
        } finally {
            tempFile.delete();
        }

    }

    */
/**
 * ensure a IllegalStateException is throw if the block size is not a power of 2
 *
 * @throws java.io.IOException
 *//*

    @Test(expected = IllegalStateException.class)
    public void checkBlockSizeIsPowerOfTwoTest() throws IOException {
        File tempFile = File.createTempFile("chronicle", "q");
        MappedFile mappedFile = new MappedFile(tempFile.getName(), 10, 0);
        new ChronicleUnsafe(mappedFile);
    }

*/
}

