/*
 * Copyright 2014 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.lang.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class VanillaMappedFileTest {
    public static String TMPDIR    = System.getProperty("java.io.tmpdir");
    public static String SEPARATOR = System.getProperty("file.separator");
    public static String BASEPATH  = TMPDIR + SEPARATOR + "vmf";

    private static File newTempraryFile(String name) {
        return newTempraryFile(name, true);
    }

    private static File newTempraryFile(String name, boolean delete) {
        File file = new File(
            BASEPATH,
            name);

        if (delete) {
            file.delete();
            file.deleteOnExit();
        }

        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return file;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testCreate() throws Exception {
        File f1 = newTempraryFile("vmf-create-1");
        File f2 = newTempraryFile("vmf-create-2");

        VanillaMappedFile vmf1 = VanillaMappedFile.readWrite(f1);
        VanillaMappedFile vmf2 = VanillaMappedFile.readWrite(f2,128);

        assertTrue(f1.exists());
        assertTrue(f2.exists());

        assertEquals(0, vmf1.size());
        assertEquals(128, vmf2.size());

        vmf1.close();
        vmf2.close();
    }

    @Test
    public void testAcquireBytes() throws Exception {
        VanillaMappedFile vmf = VanillaMappedFile.readWrite(
            newTempraryFile("vmf-acquire-buffer"));

        assertTrue(new File(vmf.path()).exists());

        VanillaMappedBytes buffer = vmf.bytes(0,128);
        assertEquals(1, buffer.refCount());
        assertEquals(0L, buffer.readLong(0L));
        assertEquals(0L, buffer.readLong(1L));

        buffer.writeLong(0, 1L);
        assertEquals(1L, buffer.readLong(0));
        assertEquals(128, buffer.size());

        buffer.writeLong(2L);
        assertEquals(2L, buffer.readLong(0));
        assertEquals(120, buffer.remaining());

        buffer.release();
        vmf.close();
    }

    @Test
    public void testAcquireBlocks1() throws Exception {
        VanillaMappedBlocks blocks = VanillaMappedBlocks.readWrite(
            newTempraryFile("vmf-acquire-blocks-1"),
            128);

        assertTrue(new File(blocks.path()).exists());

        VanillaMappedBytes b1 = blocks.acquire(0);
        assertEquals(128, blocks.size());
        assertEquals(2, b1.refCount());
        assertEquals(128, b1.size());

        VanillaMappedBytes b2 = blocks.acquire(1);
        assertEquals(256, blocks.size());
        assertEquals(2, b2.refCount());
        assertEquals(128, b2.size());

        VanillaMappedBytes b3 = blocks.acquire(1);
        assertEquals(256, blocks.size());
        assertEquals(3, b3.refCount());
        assertEquals(128, b2.size());

        assertNotEquals(b1.address(), b2.address());
        assertNotEquals(b1.address(), b3.address());
        assertEquals(b2.address(), b3.address());

        b1.release();
        b2.release();
        b2.release();

        blocks.close();
    }

    @Test
    public void testAcquireBlocks2() throws Exception {
        VanillaMappedBlocks blocks = VanillaMappedBlocks.readWrite(
            newTempraryFile("vmf-acquire-blocks-2"),
            64);

        assertTrue(new File(blocks.path()).exists());

        final long nblocks = 10000;
        for (long i = 0; i < nblocks; i++) {
            VanillaMappedBytes b = blocks.acquire(i);
            assertEquals(2, b.refCount());

            b.release();

            assertEquals(1, b.refCount());
            assertFalse(b.unmapped());
        }

        blocks.close();
    }

    @Test
    public void testAcquireOverlap() throws Exception {
        File path = newTempraryFile("vmf-acquire-overlap");

        VanillaMappedFile   vmf    = VanillaMappedFile.readWrite(path);
        VanillaMappedBlocks blocks = VanillaMappedBlocks.readWrite(path,128);

        VanillaMappedBytes b1 = blocks.acquire(0);
        b1.writeLong(1);
        b1.release();

        assertEquals(1, b1.refCount());
        assertFalse(b1.unmapped());

        VanillaMappedBytes b2 = blocks.acquire(1);
        b2.writeLong(2);
        b2.release();

        assertEquals(1, b2.refCount());
        assertFalse(b1.unmapped());
        assertFalse(b2.unmapped());

        VanillaMappedBytes b3 = blocks.acquire(2);
        b3.writeLong(3);
        b3.release();

        assertEquals(1, b3.refCount());
        assertTrue(b1.unmapped());
        assertFalse(b2.unmapped());
        assertFalse(b3.unmapped());

        VanillaMappedBytes b4 = vmf.bytes(0, 128 * 3);
        assertEquals(  1, b4.refCount());
        assertEquals(384, b4.size());
        assertEquals( 1L, b4.readLong(0));
        assertEquals( 2L, b4.readLong(128));
        assertEquals( 3L, b4.readLong(256));

        vmf.close();
        blocks.close();
    }

    @Test
    public void testReopen() throws Exception {
        File file = newTempraryFile("vmf-reopen");

        {
            VanillaMappedFile  vmf = VanillaMappedFile.readWrite(file);
            VanillaMappedBytes buf = vmf.bytes(0,128);

            buf.writeLong(0, 1L);
            
            assertEquals(1L , buf.readLong(0));
            assertEquals(128, vmf.size());

            buf.release();
            vmf.close();
        }

        {
            VanillaMappedFile  vmf = VanillaMappedFile.readWrite(file);
            VanillaMappedBytes buf = vmf.bytes(0,128);

            assertEquals(1L , buf.readLong(0));
            assertEquals(128, vmf.size());

            buf.release();
            vmf.close();
        }

        assertEquals(128, file.length());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testMappedCache1() throws Exception {
        VanillaMappedCache<Integer> cache = new VanillaMappedCache();

        assertEquals(cache.size(),0);
        assertNull(cache.get(1));

        cache.put(1,newTempraryFile("vmc-1-v1"), 64);
        cache.put(2,newTempraryFile("vmc-1-v2"),128);

        assertEquals(cache.size(),2);

        assertNotNull(cache.get(1));
        assertNotNull(cache.get(2));

        VanillaMappedBytes b1 = cache.get(1);
        assertEquals(  1, b1.refCount());
        assertEquals( 64, b1.size());

        VanillaMappedBytes b2 = cache.get(2);
        assertEquals(  1, b2.refCount());
        assertEquals(128, b2.size());

        cache.close();
    }

    @Test
    public void testMappedCache2() throws Exception {
        final int size = 5;
        VanillaMappedCache<Integer> cache = new VanillaMappedCache(size, true);
        for(int i=0;i<10;i++) {
            cache.put(i,newTempraryFile("vmc-2-v" + i),8 * i,i);
            if(i >= size) {
                assertEquals(cache.size(), size - 1);
            }
        }

        for(int i=10-1;i>=0;i--) {
            if(i >= 6) {
                assertNotNull(cache.get(i));
                assertEquals(cache.get(i).index(),i);
            } else {
                assertNull(cache.get(i));
            }
        }

        cache.close();
    }

    @Test
    public void testMappedCache3() throws Exception {
        VanillaMappedCache<Integer> cache = new VanillaMappedCache(32, false);
        VanillaMappedBytes buffer = null;
        File file = null;

        for (int j = 0; j < 5; j++) {
            long start = System.nanoTime();
            int maxRuns = 10000, runs;
            for (runs = 0; runs < maxRuns; runs++) {
                file = newTempraryFile("vmc-3-v" + runs, false);

                buffer = cache.put(runs, file, 256, runs);
                buffer.writeLong(0, 0x12345678);

                assertEquals(0x12345678L, buffer.readLong(0));
                assertEquals(runs, buffer.index());

                buffer.release();
                buffer.close();

                assertEquals(0, buffer.refCount());
                assertTrue(file.delete());
                if (System.nanoTime() - start > 1e9)
                    break;
            }

            long time = System.nanoTime() - start;
            System.out.printf("The average time was %,d us%n", time / runs / 1000);
        }

        cache.close();
    }

    @Test
    public void testMappedCache4() throws Exception {
        VanillaMappedCache<Integer> cache = new VanillaMappedCache(10000, true);
        VanillaMappedBytes buffer = cache.put(1,newTempraryFile("vmc-4"),256,1);

        buffer.reserve();
        assertEquals(2,buffer.refCount());

        buffer.release();
        assertEquals(1,buffer.refCount());

        cache.close();
        assertEquals(0,buffer.refCount());
    }
}

