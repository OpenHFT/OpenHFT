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

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for net.openhft.lang.io.VanillaMappedFile
 */
//@Ignore
public class VanillaMappedFileTest {

    private static File newTempraryFile(String name) {
        return newTempraryFile(name,true);
    }

    private static File newTempraryFile(String name,boolean delete) {
        File file = new File(System.getProperty("java.io.tmpdir"),name);
        if(delete) {
            file.delete();
            file.deleteOnExit();
        }

        return file;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testCreate() throws Exception {
        File f1 = newTempraryFile("vmf-create-1");
        File f2 = newTempraryFile("vmf-create-2");

        VanillaMappedFile vmf1 = new VanillaMappedFile(f1,VanillaMappedMode.RW);
        VanillaMappedFile vmf2 = new VanillaMappedFile(f2,VanillaMappedMode.RW,128);

        assertTrue(f1.exists());
        assertTrue(f2.exists());

        assertEquals(  0, vmf1.size());
        assertEquals(128, vmf2.size());

        vmf1.close();
        vmf2.close();
    }

    @Test
    public void testAcquireBuffer() throws Exception {
        VanillaMappedFile vmf = new VanillaMappedFile(
            newTempraryFile("vmf-acquire-buffer"),
            VanillaMappedMode.RW);

        VanillaMappedBuffer buffer = vmf.acquireOf(128);
        assertEquals( 1, buffer.refCount());
        assertEquals(0L, buffer.readLong(0L));
        assertEquals(0L, buffer.readLong(1L));

        buffer.writeLong(0,1L);
        assertEquals( 1L, buffer.readLong(0));
        assertEquals(128, buffer.size());

        buffer.writeLong(2L);
        assertEquals(2L , buffer.readLong(0));
        assertEquals(120, buffer.remaining());

        buffer.release();
        vmf.close();
    }

    @Test
    public void testAcquireBlocks() throws Exception {
        VanillaMappedFile vmf = new VanillaMappedFile(
            newTempraryFile("vmf-acquire-blocks"),
            VanillaMappedMode.RW);

        VanillaMappedBlocks blocks = vmf.blocks(128);

        VanillaMappedBuffer b1 = blocks.acquire(0);
        assertEquals(128, vmf.size());

        VanillaMappedBuffer b2 = blocks.acquire(1);
        assertEquals(256, vmf.size());

        VanillaMappedBuffer b3 = blocks.acquire(1);
        assertEquals(256, vmf.size());

        assertNotEquals(b1.address(), b2.address());
        assertNotEquals(b1.address(), b3.address());
        assertEquals(b2.address(), b3.address());

        assertEquals(128, b1.size());
        assertEquals(  1, b1.refCount());
        assertEquals(128, b2.size());
        assertEquals(  2, b2.refCount());
        assertEquals(  2, b3.refCount());

        vmf.close();
    }

    @Test
    public void testAcquireOverlap() throws Exception {
        VanillaMappedFile vmf = new VanillaMappedFile(
            newTempraryFile("vmf-acquire-overlap"),
            VanillaMappedMode.RW);

        VanillaMappedBlocks blocks = vmf.blocks(128);

        VanillaMappedBuffer b1 = blocks.acquire(0);
        b1.writeLong(1);
        b1.release();

        assertEquals(0,b1.refCount());
        assertTrue(b1.unmapped());

        VanillaMappedBuffer b2 = blocks.acquire(1);
        b2.writeLong(2);
        b2.release();

        assertEquals(0,b2.refCount());
        assertTrue(b2.unmapped());

        VanillaMappedBuffer b3 = blocks.acquire(2);
        b3.writeLong(3);
        b3.release();

        assertEquals(0,b3.refCount());
        assertTrue(b3.unmapped());

        VanillaMappedBuffer b4 = vmf.acquireAt(0,128 * 3);
        assertEquals(  1, b4.refCount());
        assertEquals(384, b4.size());
        assertEquals( 1L, b4.readLong(0));
        assertEquals( 2L, b4.readLong(128));
        assertEquals( 3L, b4.readLong(256));

        vmf.close();
    }

    @Test
    public void testReopen() throws Exception {
        File file = newTempraryFile("vmf-reopen");

        {
            VanillaMappedFile vmf = new VanillaMappedFile(
                file,
                VanillaMappedMode.RW);

            VanillaMappedBuffer buffer = vmf.acquireOf(128);
            buffer.writeLong(0, 1L);

            buffer.release();
            vmf.close();
        }

        {
            VanillaMappedFile vmf = new VanillaMappedFile(
                file,
                VanillaMappedMode.RW);

            VanillaMappedBuffer buffer = vmf.acquireOf(128);
            assertEquals(1L, buffer.readLong(0));

            buffer.release();
            vmf.close();
        }
    }

    @Test
    public void testCleanup() throws IOException, InterruptedException {
        File file = newTempraryFile("vmf-cleanup");
        File dir  = file.getParentFile();

        long free0 = dir.getFreeSpace();

        VanillaMappedFile vmf = new VanillaMappedFile(
            file,
            VanillaMappedMode.RW);

        VanillaMappedBlocks blocks = vmf.blocks(1024 * 1024);
        VanillaMappedBuffer map0 = blocks.acquire(0);
        VanillaMappedBuffer map1 = blocks.acquire(1);

        map0.position(0);
        while (map0.remaining() >= 8) {
            map0.writeLong(0x123456789ABCDEFL);
        }

        map0.position(1);
        while (map1.remaining() >= 8) {
            map1.writeLong(0x123456789ABCDEFL);
        }

        long free1 = dir.getFreeSpace();

        map0.release();
        assertEquals(0,map0.refCount());
        assertTrue(map0.unmapped());

        map1.release();
        assertEquals(0,map1.refCount());
        assertTrue(map1.unmapped());

        vmf.close();

        long free2 = dir.getFreeSpace();

        file.delete();

        long free3 = 0;
        for (int i = 0; i < 100; i++) {
            free3 = dir.getFreeSpace();

            System.out.println("Freed " + free0
                + " ~ " + free1
                + " ~ " + free2
                + " ~ " + free3
                + ", delete = " + file.delete()
            );

            if (free3 > free1) {
                break;
            }

            Thread.sleep(500);
        }

        assertTrue("free3-free1: " + (free3 - free1), free3 > free1);

    }
}
