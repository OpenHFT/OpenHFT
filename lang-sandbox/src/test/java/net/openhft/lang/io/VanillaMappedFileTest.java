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

/**
 * Tests for net.openhft.lang.io.VanillaMappedFile
 */
public class VanillaMappedFileTest {

    private static File newTempraryFile(String name) {
        return newTempraryFile(name, true);
    }

    private static File newTempraryFile(String name, boolean delete) {
        File file = new File(
            System.getProperty("java.io.tmpdir") + File.separator + "vmf",
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
        //System.out.println("Temporay directory is " + System.getProperty("java.io.tmpdir"));
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
    public void testAcquireBuffer() throws Exception {
        VanillaMappedFile vmf = VanillaMappedFile.readWrite(
            newTempraryFile("vmf-acquire-buffer"));

        VanillaMappedBuffer buffer = vmf.sliceOf(128);
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

        VanillaMappedBuffer b1 = blocks.acquire(0);
        assertEquals(128, blocks.size());

        VanillaMappedBuffer b2 = blocks.acquire(1);
        assertEquals(256, blocks.size());

        VanillaMappedBuffer b3 = blocks.acquire(1);
        assertEquals(256, blocks.size());

        assertNotEquals(b1.address(), b2.address());
        assertNotEquals(b1.address(), b3.address());
        assertEquals(b2.address(), b3.address());

        assertEquals(128, b1.size());
        assertEquals(1, b1.refCount());
        assertEquals(128, b2.size());
        assertEquals(2, b2.refCount());
        assertEquals(2, b3.refCount());

        b1.release();
        b2.release();
        b3.release();

        blocks.close();
    }

    @Test
    public void testAcquireBlocks2() throws Exception {
        VanillaMappedBlocks blocks = VanillaMappedBlocks.readWrite(
            newTempraryFile("vmf-acquire-blocks-2"),
            64);

        final long nblocks = 50 * 100 * 1000;
        for (long i = 0; i < nblocks; i++) {
            VanillaMappedBuffer b = blocks.acquire(i);
            assertEquals(1, b.refCount());

            b.release();

            assertEquals(0, b.refCount());
            assertTrue(b.unmapped());
        }

        blocks.close();
    }

    @Test
    public void testAcquireOverlap() throws Exception {
        File path = newTempraryFile("vmf-acquire-overlap");

        VanillaMappedFile   vmf    = VanillaMappedFile.readWrite(path);
        VanillaMappedBlocks blocks = VanillaMappedBlocks.readWrite(path,128);

        VanillaMappedBuffer b1 = blocks.acquire(0);
        b1.writeLong(1);
        b1.release();

        assertEquals(0, b1.refCount());
        assertTrue(b1.unmapped());

        VanillaMappedBuffer b2 = blocks.acquire(1);
        b2.writeLong(2);
        b2.release();

        assertEquals(0, b2.refCount());
        assertTrue(b2.unmapped());

        VanillaMappedBuffer b3 = blocks.acquire(2);
        b3.writeLong(3);
        b3.release();

        assertEquals(0, b3.refCount());
        assertTrue(b3.unmapped());

        VanillaMappedBuffer b4 = vmf.sliceAt(0, 128 * 3);
        assertEquals(1, b4.refCount());
        assertEquals(384, b4.size());
        assertEquals(1L, b4.readLong(0));
        assertEquals(2L, b4.readLong(128));
        assertEquals(3L, b4.readLong(256));

        vmf.close();
        blocks.close();
    }

    @Test
    public void testReopen() throws Exception {
        File file = newTempraryFile("vmf-reopen");

        {
            VanillaMappedFile   vmf = VanillaMappedFile.readWrite(file);
            VanillaMappedBuffer buf = vmf.sliceOf(128);

            buf.writeLong(0, 1L);
            assertEquals(128, vmf.size());

            buf.release();
            vmf.close();
        }

        {
            VanillaMappedFile   vmf = VanillaMappedFile.readWrite(file);
            VanillaMappedBuffer buf = vmf.sliceOf(128);

            assertEquals(1L, buf.readLong(0));
            assertEquals(128, vmf.size());

            buf.release();
            vmf.close();
        }

        assertEquals(128, file.length());
    }
}
