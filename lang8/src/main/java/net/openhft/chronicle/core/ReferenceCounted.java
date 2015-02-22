/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import java.lang.ref.WeakReference;
import java.util.List;

public interface ReferenceCounted {
    static void release(ReferenceCounted rc) {
        if (rc != null)
            rc.release();
    }

    static void releaseAll(List<WeakReference<ReferenceCounted>> refCounts) {
        for (WeakReference<? extends ReferenceCounted> refCountRef : refCounts) {
            if (refCountRef == null)
                continue;
            ReferenceCounted refCounted = refCountRef.get();
            if (refCounted != null) {
                refCounted.release();
            }
        }
    }

    void reserve() throws IllegalStateException;

    void release() throws IllegalStateException;

    long refCount();

    default boolean tryReserve() {
        try {
            if (refCount() > 0) {
                reserve();
                return true;
            }
        } catch (IllegalStateException ignored) {
        }
        return false;
    }
}