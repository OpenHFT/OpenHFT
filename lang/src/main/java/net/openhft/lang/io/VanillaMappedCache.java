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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class VanillaMappedCache<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaMappedCache.class);

    private final Map<T,DataHolder> cache;

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
            final Iterator<Map.Entry<T,DataHolder>> it = this.cache.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<T,DataHolder> entry = it.next();
                if(entry.getValue().bytes().unmapped()) {
                    entry.getValue().close();
                    it.remove();
                }
            }

            data.recycle(
                VanillaMappedFile.readWrite(path, size),
                0,
                size,
                index);

            this.cache.put(key,data);
        } catch(IOException e) {
            LOGGER.warn("",e);
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

    public synchronized void checkCounts(int min, int max) {
        for(DataHolder data : this.cache.values()) {
            if (data.bytes().refCount() < min || data.bytes().refCount() > max) {
                throw new IllegalStateException(
                    data.file().path() + " has a count of " + data.bytes().refCount());
            }
        }
    }

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

            this.file = file;
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
                this.file = null;
            } catch(IOException e) {
                LOGGER.warn("",e);
            }
        }
    }
}


