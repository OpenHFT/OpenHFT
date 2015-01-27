/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.Jvm;
import org.junit.Test;

import java.io.File;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;

public class MappedStoreTest {
    //private static final long MS_SIZE = 3L << 30;
    private static final long MS_SIZE = 1024;

    @org.junit.Ignore("TC")
    @Test
    public void testCreateSlice() throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir") + "/MappedStoreTest-testCreateSlice" + System.nanoTime() + ".tmp");
        file.deleteOnExit();
        long size = Jvm.is64Bit() ? 3L << 30 : 256 << 20;
        MappedStore ms = new MappedStore(file, FileChannel.MapMode.READ_WRITE, size);
        DirectBytes slice = ms.bytes();
        assertEquals(1, slice.refCount());
        assertEquals(0L, slice.readLong(0L));
        assertEquals(0L, slice.readLong(ms.size() - 8));

        slice.writeLong(0,1L);
        assertEquals(1L,slice.readLong(0));

        slice.release();

        ms.free();
    }

    @org.junit.Ignore("TC")
    @Test
    public void testOpenExistingFile() throws Exception {
        File file = getStoreFile("mapped-store-2.tmp");

        {
            MappedStore ms1 = new MappedStore(file, FileChannel.MapMode.READ_WRITE, MS_SIZE);
            DirectBytes slice1 = ms1.bytes();
            assertEquals(1, slice1.refCount());

            slice1.writeLong(1L);
            slice1.writeLong(2L);
            slice1.release();

            ms1.free();
        }

        {
            MappedStore ms2 = new MappedStore(file, FileChannel.MapMode.READ_WRITE, MS_SIZE);
            DirectBytes slice2 = ms2.bytes();
            assertEquals( 1, slice2.refCount());
            assertEquals(1L, slice2.readLong());
            assertEquals(2L, slice2.readLong());

            slice2.release();

            ms2.free();
        }
    }

    /*
    @org.junit.Ignore("TC") @Test
    public void testSliceSize() throws Exception {
        File file = getStoreFile("mapped-store");

        MappedStore ms = new MappedStore(file, FileChannel.MapMode.READ_WRITE, MS_SIZE);
        DirectBytes slice = ms.bytes();

        for(long i=0;i<MS_SIZE+1;i += 8) {
            slice.writeLong(i);
        }

        slice.release();
        ms.free();
    }
    */

    // *************************************************************************
    // Helpers
    // *************************************************************************

    private static File getStoreFile(String fileName) {
        File file = new File(System.getProperty("java.io.tmpdir"),fileName);
        file.delete();
        file.deleteOnExit();

        return file;
    }
}

