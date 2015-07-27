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

package net.openhft.lang.threadlocal;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ThreadLocalCopies {

    private static ThreadLocal<ThreadLocalCopies> states = new ThreadLocal<ThreadLocalCopies>() {
        @Override
        protected ThreadLocalCopies initialValue() {
            return new ThreadLocalCopies();
        }
    };

    public static ThreadLocalCopies get() {
        return states.get();
    }

    final AtomicBoolean currentlyAccessed = new AtomicBoolean(false);
    Object[] table;
    int size = 0, sizeLimit, mask;

    public ThreadLocalCopies() {
        init(32); // 16 entries
    }

    void init(int doubledCapacity) {
        table = new Object[doubledCapacity];
        sizeLimit = doubledCapacity / 3; // 0.66 fullness
        mask = (doubledCapacity - 1) & ~1;
    }

    void rehash() {
        Object[] oldTab = this.table;
        int oldCapacity = oldTab.length;
        if (oldCapacity == (1 << 30))
            throw new IllegalStateException("Hash is full");
        init(oldCapacity << 1);
        int m = mask;
        Object[] tab = this.table;
        for (int oldI = 0; oldI < oldCapacity; oldI += 2) {
            Object id = oldTab[oldI];
            if (id != null) {
                int i = System.identityHashCode(id) & m;
                while (tab[i] != null) {
                    i = (i + 2) & m;
                }
                tab[i] = id;
                tab[i + 1] = oldTab[oldI + 1];
            }
        }
    }

    void postInsert() {
        if (++size > sizeLimit)
            rehash();
    }
}
