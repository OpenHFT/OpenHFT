/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
                boolean removed = size() >= maximumCacheSize;
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

