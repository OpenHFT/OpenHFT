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

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Tests for net.openhft.lang.io.VanillaMappedFile
 */
@Ignore
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
    public void testBuilder() throws Exception {
        VanillaMappedFile vmf1 = new VanillaMappedFileBuilder()
            .path(newTempraryFile("vmf-builder-1"))
            .build();

        VanillaMappedFile vmf2 = new VanillaMappedFileBuilder()
            .path(newTempraryFile("vmf-builder-2"))
            .size(128)
            .build();

        assertEquals(  0, vmf1.size());
        assertEquals(128, vmf2.size());

        vmf1.close();
        vmf2.close();
    }

    @Test
    public void testAcquireBuffer() throws Exception {
        VanillaMappedFile vmf = new VanillaMappedFileBuilder()
            .path(newTempraryFile("vmf-acquire-buffer"))
            .readWrite()
            .build();

        VanillaMappedBuffer buffer = vmf.acquire(128);
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
        VanillaMappedFile vmf = new VanillaMappedFileBuilder()
            .path(newTempraryFile("vmf-acquire-blocks"))
            .readWrite()
            .build();

        VanillaMappedBlocks blocks = vmf.blocks(128);

        VanillaMappedBuffer b1 = blocks.acquire(0);
        assertEquals(128, vmf.size());

        VanillaMappedBuffer b2 = blocks.acquire(1);
        assertEquals(256, vmf.size());

        VanillaMappedBuffer b3 = blocks.acquire(1);
        assertEquals(256, vmf.size());

        assertEquals(128, b1.size());
        assertEquals(  1, b1.refCount());
        assertEquals(128, b2.size());
        assertEquals(  2, b2.refCount());
        assertEquals(  2, b3.refCount());

        vmf.close();
    }

    @Test
    public void testReopen() throws Exception {
        File file = newTempraryFile("vmf-reopen");

        {
            VanillaMappedFile vmf = new VanillaMappedFileBuilder()
                .path(file)
                .readWrite()
                .build();

            VanillaMappedBuffer buffer = vmf.acquire(128);
            buffer.writeLong(0, 1L);

            buffer.release();
            vmf.close();
        }

        {
            VanillaMappedFile vmf = new VanillaMappedFileBuilder()
                .path(file)
                .readWrite()
                .build();

            VanillaMappedBuffer buffer = vmf.acquire(128);
            assertEquals(1L, buffer.readLong(0));

            buffer.release();
            vmf.close();
        }
    }
}
