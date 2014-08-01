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


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class VanillaMappedCache<T> implements Closeable {
    private final boolean cleanOnClose;
    private final Map<T,VanillaMappedBytes> cache;

    public VanillaMappedCache() {
        this(new LinkedHashMap<T, VanillaMappedBytes>(),false);
    }

    public VanillaMappedCache(final boolean cleanOnClose) {
        this(new LinkedHashMap<T, VanillaMappedBytes>(), cleanOnClose);
    }

    public VanillaMappedCache(final int maximumCacheSize, boolean releaseOnRemove) {
        this(maximumCacheSize, releaseOnRemove, false);
    }

    public VanillaMappedCache(final int maximumCacheSize, final boolean releaseOnRemove, final boolean cleanOnClose) {
        this(new LinkedHashMap<T, VanillaMappedBytes>(maximumCacheSize,1.0f,true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, VanillaMappedBytes> eldest) {
                boolean removed = size() > maximumCacheSize;
                if (removed && releaseOnRemove) {
                    eldest.getValue().release();
                }

                return removed;
            }
        },
        cleanOnClose);
    }

    private VanillaMappedCache(final Map<T,VanillaMappedBytes> cache, final boolean cleanOnClose) {
        this.cache = cache;
        this.cleanOnClose = cleanOnClose;
    }

    public VanillaMappedBytes get(T key) {
        return this.cache.get(key);
    }

    public VanillaMappedBytes put(T key, File path, long size) throws IOException {
        return put(key,path,size,-1);
    }

    public VanillaMappedBytes put(T key, File path, long size, long index) throws IOException {
        VanillaMappedBytes data = this.cache.get(key);

        if(data != null) {
            if (!data.unmapped()) {
                data.cleanup();

                throw new IllegalStateException(
                    "Buffer at " + data.index() + " has a count of " + + data.refCount()
                );
            }
        }

        data = VanillaMappedFile.readWriteBytes(path,size,index);
        this.cache.put(key,data);

        return data;
    }

    public int size() {
        return this.cache.size();
    }

    @Override
    public void close() {
        final Iterator<Map.Entry<T,VanillaMappedBytes>> it = this.cache.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<T,VanillaMappedBytes> entry = it.next();
            entry.getValue().release();

            if(this.cleanOnClose && !entry.getValue().unmapped()) {
                entry.getValue().cleanup();
                entry.getValue().close();
                it.remove();
            } else  if(entry.getValue().unmapped()) {
                entry.getValue().close();
                it.remove();
            }
        }

        this.cache.clear();
    }

    public synchronized void checkCounts(int min, int max) {
        for(VanillaMappedBytes data : this.cache.values()) {
            if (data.refCount() < min || data.refCount() > max) {
                throw new IllegalStateException(
                    "Buffer at " + data.index() + " has a count of " + + data.refCount()
                );
            }
        }
    }
}


