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


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: concurrency
 */
public class VanillaMappedCache<T> {

    private final Map<T,DataHolder> cache;

    // *************************************************************************
    //
    // *************************************************************************

    public VanillaMappedCache() {
        this(new LinkedHashMap<T, DataHolder>());
    }

    public VanillaMappedCache(final int initialCapacity,final float loadFactor, final boolean accessOrder) {
        this(new LinkedHashMap<T, DataHolder>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, DataHolder> eldest) {
                boolean removed = size() >= initialCapacity;
                if (removed) {
                    eldest.getValue().close();
                }

                return removed;
            }
        });
    }

    private VanillaMappedCache(final Map<T,DataHolder> cache) {
        this.cache = cache;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public VanillaMappedBuffer get(T key) {
        DataHolder data = this.cache.get(key);
        return data != null ? data.buffer() : null;
    }

    public VanillaMappedBuffer put(T key, File path, long size) {
        DataHolder data = this.cache.get(key);
        if(data != null) {
            data.close();
        } else {
            data = new DataHolder();
        }

        try {
            data.recycle(
                VanillaMappedFile.readWrite(path, size),
                0,
                size);

            this.cache.put(key,data);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return data.buffer();
    }

    public int size() {
        return this.cache.size();
    }

    public void close() {
        for(Map.Entry<T, DataHolder> entry : this.cache.entrySet()) {
            entry.getValue().close();
        }

        this.cache.clear();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private class DataHolder {
        private VanillaMappedFile file;
        private VanillaMappedBuffer buffer;

        public DataHolder() {
            this(null,null);
        }

        public DataHolder(final VanillaMappedFile file, final VanillaMappedBuffer buffer) {
            this.file = file;
            this.buffer = buffer;
        }

        public VanillaMappedFile file() {
            return this.file;
        }

        public VanillaMappedBuffer buffer() {
            return this.buffer;
        }

        public void recycle(final VanillaMappedFile file, final VanillaMappedBuffer buffer) {
            close();

            this.file   = file;
            this.buffer = buffer;
        }

        public void recycle(final VanillaMappedFile file, long address, long size) throws IOException {
            recycle(file,file.sliceAt(address,size));
        }

        public void close()  {
            try {
                if(this.buffer != null) {
                    this.buffer.release();
                }

                if(this.file != null) {
                    this.file.close();
                }

                this.buffer = null;
                this.file = null;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}


