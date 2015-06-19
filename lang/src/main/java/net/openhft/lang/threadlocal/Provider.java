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

public abstract class Provider<T> {

    public static <T> Provider<T> of(Class<T> tClass) {
        if (StatefulCopyable.class.isAssignableFrom(tClass)) {
            return StatefulProvider.INSTANCE;
        }
        return StatelessProvider.INSTANCE;
    }

    public abstract T get(ThreadLocalCopies copies, T original);

    public abstract ThreadLocalCopies getCopies(ThreadLocalCopies copies);

    private static final class StatefulProvider<T extends StatefulCopyable<T>> extends Provider<T> {
        private static final Provider INSTANCE = new StatefulProvider();

        @Override
        public ThreadLocalCopies getCopies(ThreadLocalCopies copies) {
            if (copies != null)
                return copies;
            return ThreadLocalCopies.get();
        }

        @Override
        public T get(ThreadLocalCopies copies, T original) {
            return get(copies, original, true);
        }

        private T get(ThreadLocalCopies copies, T original, boolean syncPut) {
                Object id = original.stateIdentity();
                int m = copies.mask;
                Object[] tab = copies.table;
                int i = System.identityHashCode(id) & m;
                while (true) {
                    Object idInTable = tab[i];
                    if (idInTable == id) {
                        return (T) tab[i + 1];

                    } else if (idInTable == null) {
                        if (syncPut) {
                            if (copies.currentlyAccessed.compareAndSet(false, true)) {
                                try {
                                    return get(copies, original, false);
                                } finally {
                                    copies.currentlyAccessed.set(false);
                                }
                            } else {
                                throw new IllegalStateException("Concurrent or recursive access " +
                                        "to ThreadLocalCopies is not allowed");
                            }
                        } else {
                            // actual put
                            tab[i] = id;
                            T copy;
                            tab[i + 1] = copy = original.copy();
                            copies.postInsert();
                            return copy;
                        }
                    }
                    i = (i + 2) & m;
                }
        }
    }

    private static final class StatelessProvider<M> extends Provider<M> {
        private static final Provider INSTANCE = new StatelessProvider();

        @Override
        public M get(ThreadLocalCopies copies, M original) {
            return original;
        }

        @Override
        public ThreadLocalCopies getCopies(ThreadLocalCopies copies) {
            return copies;
        }
    }
}
