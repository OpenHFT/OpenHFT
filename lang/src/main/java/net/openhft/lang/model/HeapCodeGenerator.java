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

package net.openhft.lang.model;

enum HeapCodeGenerator implements CodeGenerator {
  /*  GET_VALUE {
        public void addCode(CodeModel codeModel, Method method) {
               StringBuilder sb = codeModel.acquireMethod()
        }
    },
    SET_VALUE {
        @Override
        public int matches() {
            return 3;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1 || method.getReturnType() == void.class)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("set") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    ADD_VALUE {
        @Override
        public int matches() {
            return 3;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("add") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    ADD_ATOMIC_VALUE {
        @Override
        public int matches() {
            return 8;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("addAtomic") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    COMPARE_AND_SWAP_VALUE {
        @Override
        public int matches() {
            return 15;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("compareAndSwap") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    TRY_LOCK_VALUE {
        @Override
        public int matches() {
            return 7;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("tryLock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    TRY_LOCK_NANOS_VALUE {
        @Override
        public int matches() {
            return 12;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("tryLockNanos") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    BUSY_LOCK_VALUE {
        @Override
        public int matches() {
            return 8;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("busyLock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    UNLOCK_VALUE {
        @Override
        public int matches() {
            return 6;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("unlock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },*/
}
