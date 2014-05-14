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

    public VanillaMappedCache(final int maximumCacheSize, final boolean cleanOnRemove) {
        this(new LinkedHashMap<T, DataHolder>(maximumCacheSize,1.0f,true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, DataHolder> eldest) {
                boolean removed = size() >= maximumCacheSize;
                if (removed && cleanOnRemove) {
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

    public VanillaMappedBytes get(T key) {
        DataHolder data = this.cache.get(key);
        return data != null ? data.bytes() : null;
    }

    public VanillaMappedBytes put(T key, File path, long size) {
        return put(key,path,size,-1);
    }

    public VanillaMappedBytes put(T key, File path, long size, long index) {
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
                size,
                index);

            this.cache.put(key,data);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return data.bytes();
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
        private VanillaMappedBytes bytes;

        public DataHolder() {
            this(null,null);
        }

        public DataHolder(final VanillaMappedFile file, final VanillaMappedBytes bytes) {
            this.file = file;
            this.bytes = bytes;
        }

        public VanillaMappedFile file() {
            return this.file;
        }

        public VanillaMappedBytes bytes() {
            return this.bytes;
        }

        public void recycle(final VanillaMappedFile file, final VanillaMappedBytes bytes) {
            close();

            this.file   = file;
            this.bytes = bytes;
        }

        public void recycle(final VanillaMappedFile file, long address, long size) throws IOException {
            recycle(file,file.bytes(address,size));
        }

        public void recycle(final VanillaMappedFile file, long address, long size, long index) throws IOException {
            recycle(file,file.bytes(address,size,index));
        }

        public void close()  {
            try {
                if(this.bytes != null) {
                    this.bytes.release();
                }

                if(this.file != null) {
                    this.file.close();
                }

                this.bytes = null;
                this.file  = null;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}


