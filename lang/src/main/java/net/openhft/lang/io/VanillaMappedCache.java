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

    private final Map<T,VanillaMappedBytes> cache;

    public VanillaMappedCache() {
        this(new LinkedHashMap<T, VanillaMappedBytes>());
    }

    public VanillaMappedCache(final int maximumCacheSize, final boolean cleanOnRemove) {
        this(new LinkedHashMap<T, VanillaMappedBytes>(maximumCacheSize,1.0f,true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, VanillaMappedBytes> eldest) {
                boolean removed = size() >= maximumCacheSize;
                if (removed && cleanOnRemove) {
                    eldest.getValue().close();
                }

                return removed;
            }
        });
    }

    private VanillaMappedCache(final Map<T,VanillaMappedBytes> cache) {
        this.cache = cache;
    }

    public VanillaMappedBytes get(T key) {
        return this.cache.get(key);
    }

    public VanillaMappedBytes put(T key, File path, long size) {
        return put(key,path,size,-1);
    }

    public VanillaMappedBytes put(T key, File path, long size, long index) {
        VanillaMappedBytes data = this.cache.get(key);

        try {
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
        } catch(IOException e) {
            LOGGER.warn("",e);
        }

        return data;
    }

    public int size() {
        return this.cache.size();
    }

    public void close() {
        final Iterator<Map.Entry<T,VanillaMappedBytes>> it = this.cache.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<T,VanillaMappedBytes> entry = it.next();
            entry.getValue().release();

            if(entry.getValue().unmapped()) {
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


